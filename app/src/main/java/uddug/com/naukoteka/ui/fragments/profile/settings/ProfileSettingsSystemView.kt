package uddug.com.naukoteka.ui.fragments.profile.settings

import moxy.MvpView
import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType
import uddug.com.domain.entities.profile.UserProfileFullInfo
import uddug.com.naukoteka.global.views.InformativeView
import uddug.com.naukoteka.global.views.LoadingView

@StateStrategyType(OneExecutionStateStrategy::class)
interface ProfileSettingsSystemView : MvpView, LoadingView, InformativeView {
    fun setMainInformation(profileInfo: UserProfileFullInfo)
    fun showClearCacheSuccess()
    fun setCompressImage(compress: Boolean)
    fun setCompressVideo(compress: Boolean)
    fun setAutoPlayGif(autoPlay: Boolean)
    fun setAutoplayVideo(autoPlay: Boolean)
    fun setThemeMode(themeMode: ThemeMode)
    fun clearCache()
    fun setEnvironment(environment: AppEnvironmentMode)
}

enum class ThemeMode {
    LIGHT,
    DARK
}

enum class AppEnvironmentMode {
    DEV,
    PROD
}
