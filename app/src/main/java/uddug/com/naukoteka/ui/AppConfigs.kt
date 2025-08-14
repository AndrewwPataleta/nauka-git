package uddug.com.naukoteka.ui

import uddug.com.domain.entities.PasswordRequirementsEntity
import uddug.com.domain.repositories.auth.models.SocialType
import uddug.com.naukoteka.BuildConfig
import uddug.com.naukoteka.R

object AppConfigs {

    fun authSocialUrl(socialType: SocialType) =
        "https://stage.naukotheka.ru/api/a/oauth2/authorize/nkt?idp=${socialType.raw}&redirect_uri=${redirectSocialUrl(socialType)}"

    fun redirectSocialUrl(socialType: SocialType) = "https://stage.naukotheka.ru/$SOCIAL_LOGIN_ENDPOINT?social_type=${socialType.raw}"

    const val SOCIAL_LOGIN_ENDPOINT = "login-actions/social-login"

    val TIMEOUT_TO_RESEND = if (BuildConfig.DEBUG) 10 else 120

    fun getPasswordRequirements() = listOf(
        PasswordRequirementsEntity(R.string.dialog_password_requirements_count_of_symbols),
        PasswordRequirementsEntity(R.string.dialog_password_requirements_capital_letters),
        PasswordRequirementsEntity(R.string.dialog_password_requirements_digits),
        PasswordRequirementsEntity(R.string.dialog_password_requirements_symbols)
    )

}