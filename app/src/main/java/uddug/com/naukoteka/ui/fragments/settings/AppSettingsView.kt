package uddug.com.naukoteka.ui.fragments.settings

import uddug.com.naukoteka.global.views.InformativeView
import uddug.com.naukoteka.global.views.LoadingView
import moxy.MvpView
import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType
import uddug.com.domain.entities.country.Country
import uddug.com.domain.entities.country.Settlement
import uddug.com.domain.entities.profile.FormContainer
import uddug.com.domain.entities.profile.UserProfileFullInfo
import uddug.com.naukoteka.presentation.profile.edit.models.CountryType
import uddug.com.naukoteka.presentation.profile.edit.models.SettlementType

@StateStrategyType(OneExecutionStateStrategy::class)
interface AppSettingsView : MvpView, LoadingView, InformativeView {

    fun openProfileSecure(profileInfo: UserProfileFullInfo)

    fun setUserDynamicNavigationForms(formContainer: List<FormContainer>)

    fun openDynamicNavigationContainer(
        formContainer: FormContainer,
        userProfileFullInfo: UserProfileFullInfo
    )

    fun openSystemSettings(profileInfo: UserProfileFullInfo)
}
