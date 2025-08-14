package uddug.com.naukoteka.presentation.registration_third_step

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import uddug.com.domain.interactors.auth.AuthInteractor
import uddug.com.naukoteka.di.modules.SimpleRegistration
import uddug.com.naukoteka.global.base.BasePresenterImpl
import uddug.com.naukoteka.navigation.AppRouter
import uddug.com.naukoteka.navigation.Screens
import uddug.com.naukoteka.ui.AppConfigs
import moxy.InjectViewState
import toothpick.InjectConstructor
import java.util.concurrent.TimeUnit

@InjectConstructor
@InjectViewState
class RegistrationThirdStepPresenter(
    val router: AppRouter,
    private val simpleRegistration: SimpleRegistration,
    private val authInteractor: AuthInteractor,
) : BasePresenterImpl<RegistrationThirdStepView>() {

    private var countDownTimer: Disposable? = null

    override fun attachView(view: RegistrationThirdStepView?) {
        super.attachView(view)
        startTimerResendCode()
    }

    fun resendCode() {
        authInteractor.register(simpleRegistration.login!!, simpleRegistration.password!!)
            .await {
                startTimerResendCode()
            }
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

    fun checkKey(key: String) {
        authInteractor.checkToken(key)
            .await {
                router.newRootChain(Screens.Login(), Screens.SignedUpFinished())
            }
    }
}