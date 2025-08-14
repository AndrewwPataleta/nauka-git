package uddug.com.naukoteka.presentation.registration_second_step

import android.content.Context
import uddug.com.data.validator.FieldsValidator
import uddug.com.domain.entities.HttpException
import uddug.com.domain.entities.ServerApiError
import uddug.com.domain.interactors.auth.AuthInteractor
import uddug.com.naukoteka.R
import uddug.com.naukoteka.di.modules.SimpleRegistration
import uddug.com.naukoteka.global.base.BasePresenterImpl
import uddug.com.naukoteka.navigation.AppRouter
import uddug.com.naukoteka.navigation.Screens
import uddug.com.naukoteka.ui.custom.square_toast.SquareToast
import uddug.com.naukoteka.ui.custom.square_toast.ToastInfo
import moxy.InjectViewState
import toothpick.InjectConstructor

@InjectConstructor
@InjectViewState
class RegistrationSecondStepPresenter(
    private val fieldsValidator: FieldsValidator,
    private val router: AppRouter,
    private val simpleRegistration: SimpleRegistration,
    private val authInteractor: AuthInteractor,
    private val context: Context,
) : BasePresenterImpl<RegistrationSecondStepView>() {


    fun isValidFields(password: String, passwordConfirmation: String) {
        viewState.showButtonState(
            fieldsValidator.isNotEmpty(password) &&
                    fieldsValidator.isNotEmpty(passwordConfirmation) &&
                    fieldsValidator.isEquals(password, passwordConfirmation)
        )
    }

    fun exit() {
        router.exit()
    }

    fun nextStep(password: String) {
        simpleRegistration.password = password
        authInteractor.register(simpleRegistration.login!!, simpleRegistration.password!!)
            .await(
                onComplete = { router.navigateTo(Screens.RegistrationThirdStep()) },
                onError = {
                    if (it is HttpException && it.statusCode == ServerApiError.PasswordPolicy) {
                        viewState.showMessage(
                            ToastInfo(
                                context.getString(R.string.error_password_policy),
                                type = SquareToast.Type.ERROR
                            )
                        )
                    } else onError(it)
                })

    }
}