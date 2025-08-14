package uddug.com.naukoteka.ui.fragments.profile.settings

import moxy.InjectViewState
import toothpick.InjectConstructor
import uddug.com.data.cache.model.UserSystemSettings
import uddug.com.data.cache.model.UserTheme
import uddug.com.data.cache.system_settings.UserSystemSettingsCache
import uddug.com.domain.entities.profile.UserProfileFullInfo
import uddug.com.domain.interactors.user_profile.UserProfileInteractor
import uddug.com.naukoteka.global.base.BasePresenterImpl

@InjectConstructor
@InjectViewState
class ProfileSettingsSystemPresenter(
    private val userProfileInteractor: UserProfileInteractor,
    private val userSystemSettingsCache: UserSystemSettingsCache
) : BasePresenterImpl<ProfileSettingsSystemView>() {

    var userProfileFullInfo: UserProfileFullInfo? = null

    init {
        if (userSystemSettingsCache.entity == null) {
            userSystemSettingsCache.entity = UserSystemSettings()
        }
    }

    fun setProfileFullInfo(profileFullInfo: UserProfileFullInfo) {
        this.userProfileFullInfo = profileFullInfo
        viewState.setMainInformation(profileFullInfo)
        updateUserSettingsDisplay()
    }

    private fun updateUserSettingsDisplay() {
        userSystemSettingsCache.entity?.let {
            viewState.setCompressImage(it.compressImage)
            viewState.setCompressVideo(it.compressVideo)
            viewState.setAutoPlayGif(it.autoPlayGif)
            viewState.setAutoplayVideo(it.autoPlayVideo)
        }

    }

    fun selectDarkMode() {
        userSystemSettingsCache.entity = userSystemSettingsCache.entity?.copy(
            theme = UserTheme.DARK
        )
        viewState.setThemeMode(ThemeMode.DARK)
    }

    fun selectLightMode() {
        userSystemSettingsCache.entity = userSystemSettingsCache.entity?.copy(
            theme = UserTheme.LIGHT
        )
        viewState.setThemeMode(ThemeMode.LIGHT)

    }

    fun selectClearCache() {
        viewState.clearCache()
        userSystemSettingsCache.entity = UserSystemSettings()
        updateUserSettingsDisplay()
    }

    fun selectCompressImage(compress: Boolean) {
        userSystemSettingsCache.entity = userSystemSettingsCache.entity?.copy(
            compressImage = compress
        )
    }

    fun selectCompressVideoSwitch(compress: Boolean) {
        userSystemSettingsCache.entity = userSystemSettingsCache.entity?.copy(
            compressVideo = compress
        )
    }

    fun selectAutoPlayGif(compress: Boolean) {
        userSystemSettingsCache.entity = userSystemSettingsCache.entity?.copy(
            autoPlayGif = compress
        )
    }

    fun selectAutoPlayVideoSwitch(compress: Boolean) {
        userSystemSettingsCache.entity = userSystemSettingsCache.entity?.copy(
            autoPlayVideo = compress
        )
    }

}
