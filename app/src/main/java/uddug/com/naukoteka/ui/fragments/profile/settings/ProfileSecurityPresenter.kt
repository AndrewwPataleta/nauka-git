package uddug.com.naukoteka.ui.fragments.profile.settings

import moxy.InjectViewState
import toothpick.InjectConstructor
import uddug.com.data.cache.cookies.CookiesCache
import uddug.com.data.cache.user_id.UserIdCache
import uddug.com.domain.entities.profile.UserProfileFullInfo
import uddug.com.domain.interactors.user_profile.UserProfileInteractor
import uddug.com.naukoteka.global.base.BasePresenterImpl

@InjectConstructor
@InjectViewState
class ProfileSecurityPresenter(
    private val userProfileInteractor: UserProfileInteractor,
    private val cookiesCache: CookiesCache,
    private val userIdCache: UserIdCache
) : BasePresenterImpl<ProfileSecurityView>() {

    var userProfileFullInfo: UserProfileFullInfo? = null

    fun setProfileFullInfo(profileFullInfo: UserProfileFullInfo) {
        this.userProfileFullInfo = profileFullInfo
        viewState.setMainInformation(profileFullInfo)
    }

    fun selectExitProfile() {
        viewState.openLogoutDialog()
    }

    fun selectConfirmExit() {
        cookiesCache.clear()
        userIdCache.clear()
        viewState.openLoginPage()
    }

    fun selectDeleteAccount() {
        viewState.openDeleteAccountDialog()
    }

    fun selectConfirmDelete(reason: String? = null) {
        viewState.showFrozenAccount()
    }

    fun selectChangePassword() {
        viewState.openChangePasswordDialog()
    }

}
