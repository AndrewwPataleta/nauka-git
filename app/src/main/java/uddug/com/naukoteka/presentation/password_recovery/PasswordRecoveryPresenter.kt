package uddug.com.naukoteka.presentation.password_recovery

import android.content.Context
import android.os.Handler
import android.os.Looper
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import uddug.com.data.validator.FieldsValidator
import uddug.com.domain.entities.HttpException
import uddug.com.domain.entities.ServerApiError
import uddug.com.domain.interactors.auth.AuthInteractor
import uddug.com.domain.repositories.auth.models.SessionAttributes
import uddug.com.naukoteka.R
import uddug.com.naukoteka.global.base.BasePresenterImpl
import uddug.com.naukoteka.navigation.AppRouter
import uddug.com.naukoteka.navigation.Screens
import uddug.com.naukoteka.ui.AppConfigs
import uddug.com.naukoteka.ui.custom.square_toast.SquareToast
import uddug.com.naukoteka.ui.custom.square_toast.ToastInfo
import moxy.InjectViewState
import toothpick.InjectConstructor
import java.util.concurrent.TimeUnit

@InjectConstructor
@InjectViewState
class PasswordRecoveryPresenter(
    private val fieldsValidator: FieldsValidator,
    val router: AppRouter,
    private val authInteractor: AuthInteractor,
    private val context: Context
) : BasePresenterImpl<PasswordRecoveryView>() {

    private var sessionAttrs: SessionAttributes? = null
    private var countDownTimer: Disposable? = null

    fun isValidEmail(email: String) {
        viewState.showButtonState(
            fieldsValidator.isValidEmail(email)
        )
    }

    fun isValidPasswords(password: String, passwordConfirmation: String) {
        viewState.showButtonStateOfPasswordRecoveryVerification(
            fieldsValidator.isEquals(password, passwordConfirmation)
        )
    }

    fun startTimerResendCode() {
        var countSeconds = AppConfigs.TIMEOUT_TO_RESEND
        countDownTimer?.dispose()
        countDownTimer = Observable.interval(1000, TimeUnit.MILLISECONDS)
            .map { countSeconds-- }
            .observeOn(AndroidSchedulers.mainThread())
            .await(withProgress = false) { seconds ->
                if (seconds <= 0) {
                    countDownTimer?.dispose()
                }
                viewState.showSecondsToResend(seconds)
            }
    }

    fun exit() {
        router.exit()
    }

    fun toAuthorization() {
        router.backTo(Screens.Login())
    }

    fun recoveryPassword(email: String) {
        authInteractor.sendLetterToRecovery(email).await(onError = {
            if (it is HttpException && it.statusCode == ServerApiError.InvalidUser) {
                viewState.showErrorEmail(false)
            } else onError(it)
        }) {
            viewState.showTimer()
        }
    }

    fun checkKey(key: String) {
        authInteractor.checkTokenToRecovery(key)
            .await {
                sessionAttrs = it
                viewState.showInputNewPassword()
            }
    }

    fun changePassword(password: String) {
        sessionAttrs?.let { sessionAttrs ->
            authInteractor.setNewPassword(password, sessionAttrs)
                .await(onError = {
                    if (it is HttpException && it.statusCode == ServerApiError.PasswordPolicy) {
                        viewState.showMessage(
                            ToastInfo(
                                context.getString(R.string.error_password_policy),
                                type = SquareToast.Type.ERROR
                            )
                        )
                    } else onError(it)
                }) {
                    viewState.startDialogPasswordRecoverySuccessful()
                    Handler(Looper.getMainLooper()).postDelayed({
                        router.newRootScreen(Screens.Login())
                    }, 2000L)
                }
        }
    }

}