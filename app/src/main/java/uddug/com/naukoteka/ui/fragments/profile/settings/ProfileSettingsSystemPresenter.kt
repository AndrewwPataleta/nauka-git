package uddug.com.naukoteka.ui.fragments.profile.settings

import moxy.InjectViewState
import toothpick.InjectConstructor
import uddug.com.data.cache.model.UserSystemSettings
import uddug.com.data.cache.model.UserTheme
import uddug.com.data.cache.system_settings.UserSystemSettingsCache
import uddug.com.domain.entities.profile.UserProfileFullInfo
import uddug.com.domain.interactors.user_profile.UserProfileInteractor
import uddug.com.naukoteka.environment.AppEnvironment
import uddug.com.naukoteka.environment.EnvironmentSwitcherService
import uddug.com.naukoteka.global.base.BasePresenterImpl

@InjectConstructor
@InjectViewState
class ProfileSettingsSystemPresenter(
    private val userProfileInteractor: UserProfileInteractor,
    private val userSystemSettingsCache: UserSystemSettingsCache,
    private val environmentSwitcherService: EnvironmentSwitcherService,
) : BasePresenterImpl<ProfileSettingsSystemView>() {

    var userProfileFullInfo: UserProfileFullInfo? = null

    init {
        if (userSystemSettingsCache.entity == null) {
            userSystemSettingsCache.entity = UserSystemSettings()
        }
    }

    fun setProfileFullInfo(profileFullInfo: UserProfileFullInfo) {
        userProfileFullInfo = profileFullInfo
        viewState.setMainInformation(profileFullInfo)
        pushSettingsToView()
    }

    private fun pushSettingsToView() {
        val settings = userSystemSettingsCache.entity ?: return
        viewState.setEnvironment(environmentSwitcherService.getCurrentEnvironment().toViewMode())
        viewState.setCompressImage(settings.compressImage)
        viewState.setCompressVideo(settings.compressVideo)
        viewState.setAutoPlayGif(settings.autoPlayGif)
        viewState.setAutoplayVideo(settings.autoPlayVideo)
    }

    fun selectDarkMode() {
        userSystemSettingsCache.entity = userSystemSettingsCache.entity?.copy(theme = UserTheme.DARK)
        viewState.setThemeMode(ThemeMode.DARK)
    }

    fun selectLightMode() {
        userSystemSettingsCache.entity = userSystemSettingsCache.entity?.copy(theme = UserTheme.LIGHT)
        viewState.setThemeMode(ThemeMode.LIGHT)
    }

    fun selectClearCache() {
        viewState.clearCache()
        userSystemSettingsCache.entity = UserSystemSettings()
        pushSettingsToView()
    }

    fun selectCompressImage(enabled: Boolean) {
        userSystemSettingsCache.entity = userSystemSettingsCache.entity?.copy(compressImage = enabled)
    }

    fun selectCompressVideoSwitch(enabled: Boolean) {
        userSystemSettingsCache.entity = userSystemSettingsCache.entity?.copy(compressVideo = enabled)
    }

    fun selectAutoPlayGif(enabled: Boolean) {
        userSystemSettingsCache.entity = userSystemSettingsCache.entity?.copy(autoPlayGif = enabled)
    }

    fun selectAutoPlayVideoSwitch(enabled: Boolean) {
        userSystemSettingsCache.entity = userSystemSettingsCache.entity?.copy(autoPlayVideo = enabled)
    }

    fun selectEnvironment(isDev: Boolean) {
        val environment = if (isDev) AppEnvironment.DEV else AppEnvironment.PROD
        environmentSwitcherService.updateEnvironment(environment)
        viewState.setEnvironment(environment.toViewMode())
    }

    private fun AppEnvironment.toViewMode(): AppEnvironmentMode = when (this) {
        AppEnvironment.DEV -> AppEnvironmentMode.DEV
        AppEnvironment.PROD -> AppEnvironmentMode.PROD
    }
}
