package uddug.com.naukoteka.ui.fragments.settings

import com.google.gson.JsonElement
import uddug.com.naukoteka.global.views.InformativeView
import uddug.com.naukoteka.global.views.LoadingView
import moxy.MvpView
import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType
import uddug.com.domain.entities.country.Country
import uddug.com.domain.entities.country.Settlement
import uddug.com.domain.entities.profile.FormContainer
import uddug.com.domain.entities.profile.UserProfileFullInfo
import uddug.com.domain.interactors.user_profile.model.DefaultCls
import uddug.com.naukoteka.presentation.profile.edit.models.CountryType
import uddug.com.naukoteka.presentation.profile.edit.models.SettlementType

@StateStrategyType(OneExecutionStateStrategy::class)
interface DynamicSettingsView : MvpView, LoadingView, InformativeView {

    fun setDynamicSettingsForm(
        formContainer: FormContainer,
        clsList: List<DefaultCls>,
        settings: MutableMap<String, String>
    )

    fun showToastUpdate(toastStatus: ToastStatus)

}

enum class ToastStatus {
    SUCCESS,
    FAIL
}
