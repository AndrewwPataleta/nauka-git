package uddug.com.naukoteka.presentation.education

import uddug.com.naukoteka.global.views.InformativeView
import uddug.com.naukoteka.global.views.LoadingView
import moxy.MvpView
import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType
import uddug.com.domain.entities.profile.Education

@StateStrategyType(OneExecutionStateStrategy::class)
interface EducationAdditionalActionView : MvpView, LoadingView, InformativeView {

    fun openCountrySelectPage(selectedCountryId: String?)

    fun setCurrentEducationInfo(education: Education)

    fun educationSuccessUpdated()
}
