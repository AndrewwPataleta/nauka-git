package uddug.com.naukoteka.presentation.profile

import uddug.com.data.cache.cookies.CookiesCache
import uddug.com.data.cache.user_id.UserIdCache
import uddug.com.domain.interactors.user_profile.UserProfileInteractor
import uddug.com.naukoteka.global.base.BasePresenterImpl
import moxy.InjectViewState
import toothpick.InjectConstructor
import uddug.com.domain.entities.profile.UserProfileFullInfo

@InjectConstructor
@InjectViewState
class ProfileAdditionalActionPresenter(
    private val userProfileInteractor: UserProfileInteractor,
    private val cookiesCache: CookiesCache,
    private val userIdCache: UserIdCache,
) : BasePresenterImpl<ProfileAdditionalActionView>() {

    private var profileFullInfo: UserProfileFullInfo? = null

    fun setProfileFullInfo(profileFullInfo: UserProfileFullInfo) {
        this.profileFullInfo = profileFullInfo
    }

    fun selectShowProfileEdit() {
        profileFullInfo?.let { viewState.openProfileEditFragment(it) }
    }

    fun selectShowProfileImage() {
        profileFullInfo?.let { viewState.openProfilePhotoImageView(it) }
    }

    fun selectCopyToClipboard() {
        profileFullInfo?.nickname?.let { viewState.copyToClipboardAndClose(it) }
    }

    fun selectOpenAppSettings() {
        profileFullInfo?.let { viewState.openAppProfileSettings(it) }
    }

    fun selectOpenHelpWithSupport() {
        profileFullInfo?.let { viewState.helpWithSupport(it) }
    }

    fun selectExitProfile() {
        viewState.openLogoutDialog()
    }

    fun selectConfirmExit() {
        cookiesCache.clear()
        userIdCache.clear()
        viewState.openLoginPage()
    }
}
