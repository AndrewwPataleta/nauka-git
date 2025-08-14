package uddug.com.naukoteka.presentation.registration_first_step

import uddug.com.data.validator.FieldsValidator
import uddug.com.domain.interactors.auth.AuthInteractor
import uddug.com.naukoteka.di.DI
import uddug.com.naukoteka.di.modules.RegistrationModule
import uddug.com.naukoteka.di.modules.SimpleRegistration
import uddug.com.naukoteka.navigation.AppRouter
import uddug.com.naukoteka.navigation.Screens
import uddug.com.naukoteka.global.base.SocialLoginPresenter
import moxy.InjectViewState
import toothpick.InjectConstructor
import toothpick.ktp.KTP

@InjectConstructor
@InjectViewState
class RegistrationFirstStepPresenter(
    private val fieldsValidator: FieldsValidator,
    private val simpleRegistration: SimpleRegistration,
    override val router: AppRouter,
    override val authInteractor: AuthInteractor,
) : SocialLoginPresenter<RegistrationFirstStepView>() {

    init {
        val scope = getScope().openSubScope(DI.REGISTRATION_SCOPE)
        scope.installModules(RegistrationModule())
    }

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        authInteractor.getSocialTypes()
            .await(withProgress = false) { viewState.showSocialTypes(it) }
    }

    fun isValidField(email: String) {
        viewState.showButtonState(
            fieldsValidator.isValidEmail(email)
        )
    }

    fun exit() {
        KTP.closeScope(DI.REGISTRATION_SCOPE)
        router.exit()
    }

    fun nextStep(login: String) {
        authInteractor.checkEmailFree(login)
            .await {
                simpleRegistration.login = login
                router.navigateTo(Screens.RegistrationSecondStep())
            }
    }
}