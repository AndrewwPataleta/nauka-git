package uddug.com.naukoteka.presentation.login

import android.app.Activity
import android.content.Intent
import com.franmontiel.localechanger.LocaleChanger
import uddug.com.data.validator.FieldsValidator
import uddug.com.domain.entities.HttpException
import uddug.com.domain.entities.ServerApiError
import uddug.com.domain.interactors.auth.AuthInteractor
import uddug.com.domain.utils.logging.ILogger
import uddug.com.naukoteka.ext.data.CURRENTLY_ADDED_LANGUAGES
import uddug.com.naukoteka.ext.data.SUPPORTED_LOCALES_CUSTOM
import uddug.com.naukoteka.global.base.SocialLoginPresenter
import uddug.com.naukoteka.navigation.AppRouter
import uddug.com.naukoteka.navigation.Screens
import uddug.com.naukoteka.ui.activities.main.AuthActivity
import uddug.com.naukoteka.ui.fragments.login.CustomLanguage
import moxy.InjectViewState
import toothpick.InjectConstructor
import java.util.*
import kotlin.collections.ArrayList


@InjectConstructor
@InjectViewState
class LoginPresenter(
    override val authInteractor: AuthInteractor,
    private val fieldsValidator: FieldsValidator,
    override val router: AppRouter,
    private val logger: ILogger
) : SocialLoginPresenter<LoginView>() {

    var activity: Activity? = null

    lateinit var filterList: MutableList<CustomLanguage>

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        authInteractor.getSocialTypes()
            .await(withProgress = false) { viewState.showSocialTypes(it) }
    }

    override fun attachView(view: LoginView?) {
        super.attachView(view)
        filterList = ArrayList()
        viewState.showLanguages(SUPPORTED_LOCALES_CUSTOM.filter {
            CURRENTLY_ADDED_LANGUAGES.contains(it.locale)
        })
    }

    fun onLanguageChange(locale: Locale) {
        LocaleChanger.resetLocale()
        LocaleChanger.setLocale(locale)
        activity?.finishAffinity()
        activity?.startActivity(Intent(activity, AuthActivity::class.java))
    }

    fun isValidFields(email: String, password: String) {
        viewState.showButtonState(
            fieldsValidator.isValidEmail(email) &&
                    fieldsValidator.isNotEmpty(password)
        )
    }

    fun enter(email: String, pass: String, rememberAuth: Boolean) {
        authInteractor.login(email, pass)
            .await(onError = {
                if (it is HttpException && it.statusCode == ServerApiError.InvalidCredentials) {
                    viewState.showErrorCredentials()
                } else onError(it)
            }, onComplete = { viewState.showSuccessLogin() })
    }

    fun toRegistration() {
        logger.debug("click toRegistration")
        router.navigateTo(Screens.RegistrationFirstStep())
    }

    fun toPasswordRecovery() {
        logger.debug("click toPasswordRecovery")
        router.navigateTo(Screens.PasswordRecovery())
    }

    fun exit() {
        router.exit()
    }

    fun enterTest() {
        authInteractor.testLogin()
            .await(onError = {
                if (it is HttpException && it.statusCode == ServerApiError.InvalidCredentials) {
                    viewState.showErrorCredentials()
                } else onError(it)
            }, onComplete = { viewState.showSuccessLogin() })
    }


}
