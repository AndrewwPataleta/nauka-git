package uddug.com.naukoteka.presentation.profile.edit

import uddug.com.naukoteka.global.views.InformativeView
import uddug.com.naukoteka.global.views.LoadingView
import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType
import uddug.com.domain.entities.country.Country
import uddug.com.domain.entities.country.Settlement
import uddug.com.domain.entities.profile.UserProfileFullInfo

@StateStrategyType(OneExecutionStateStrategy::class)
interface ProfileEditAddressesListView : MvpView, LoadingView, InformativeView {
    fun setMainInformation(profileInfo: UserProfileFullInfo)

    fun setCountryBord(country: Country)
    fun setSettlementBord(settlement: String)
    fun setCountryLive(country: Country)
    fun setSettlementLive(settlement: String)

    fun openSelectCountryForBorn(country: Country)
    fun openSettlementForBorn(country: Country, settlement: String?)
    fun openSelectCountryForLive(country: Country)
    fun openSettlementForLive(country: Country, settlement: String?)
    fun showSuccessToast()
}
