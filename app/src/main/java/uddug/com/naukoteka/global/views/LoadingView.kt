package uddug.com.naukoteka.global.views

import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType

interface LoadingView: MvpView {

    @StateStrategyType(AddToEndSingleStrategy::class)
    fun showLoading(show: Boolean)

    @StateStrategyType(AddToEndSingleStrategy::class)
    fun showRefreshLoading(show: Boolean)
}