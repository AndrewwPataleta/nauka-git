package uddug.com.naukoteka.presentation.tutorial

import uddug.com.domain.entities.TutorialEntity
import uddug.com.naukoteka.global.views.InformativeView
import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(AddToEndSingleStrategy::class)
interface TutorialView : MvpView, InformativeView {
    @StateStrategyType(OneExecutionStateStrategy::class)
    fun showTutorialInformation(resources: List<TutorialEntity>)
}