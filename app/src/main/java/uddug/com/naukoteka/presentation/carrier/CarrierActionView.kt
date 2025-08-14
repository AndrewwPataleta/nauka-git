package uddug.com.naukoteka.presentation.carrier

import uddug.com.naukoteka.global.views.InformativeView
import uddug.com.naukoteka.global.views.LoadingView
import moxy.MvpView
import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType
import uddug.com.domain.entities.country.Settlement
import uddug.com.domain.entities.profile.Education
import uddug.com.domain.entities.profile.LaborActivities

@StateStrategyType(OneExecutionStateStrategy::class)
interface CarrierActionView : MvpView, LoadingView, InformativeView {

    fun openCountrySelectPage(selectedCountryId: String?)

    fun setCurrentCarrierInfo(education: LaborActivities)

    fun carrierSuccessUpdated()

    fun setSettlements(settlements: List<Settlement>)

    fun showUpdateValidationError()

    fun showCreateValidationError()
}
