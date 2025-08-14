package uddug.com.naukoteka.ui.fragments.splash

import uddug.com.naukoteka.global.views.InformativeView
import uddug.com.naukoteka.global.views.LoadingView
import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType
import uddug.com.domain.entities.profile.UserProfileFullInfo

@StateStrategyType(AddToEndSingleStrategy::class)
interface SplashView : MvpView, LoadingView, InformativeView {

    fun showContainerActivity()
    fun showAuthActivity()
    fun showUserTheme(userTheme: UserTheme)
}

enum class UserTheme {
    LIGHT,
    DARK
}
