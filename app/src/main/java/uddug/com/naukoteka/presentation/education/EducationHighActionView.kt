package uddug.com.naukoteka.presentation.education

import android.widget.ArrayAdapter
import uddug.com.naukoteka.global.views.InformativeView
import uddug.com.naukoteka.global.views.LoadingView
import moxy.MvpView
import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType
import uddug.com.domain.entities.country.Settlement
import uddug.com.domain.entities.profile.Education

@StateStrategyType(OneExecutionStateStrategy::class)
interface EducationHighActionView : MvpView, LoadingView, InformativeView {

    fun openCountrySelectPage(selectedCountryId: String?)

    fun setCurrentEducationInfo(education: Education)

    fun educationSuccessUpdated()

    fun setSettlements(settlements: List<Settlement>)

    fun showUpdateValidationError()

    fun showCreateValidationError()
}
