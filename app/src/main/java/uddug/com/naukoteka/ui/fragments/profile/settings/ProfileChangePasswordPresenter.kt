package uddug.com.naukoteka.ui.fragments.profile.settings

import moxy.InjectViewState
import toothpick.InjectConstructor
import uddug.com.domain.entities.profile.UserProfileFullInfo
import uddug.com.domain.interactors.user_profile.UserProfileInteractor
import uddug.com.naukoteka.global.base.BasePresenterImpl
import uddug.com.naukoteka.utils.text.isNotNullOrEmpty

@InjectConstructor
@InjectViewState
class ProfileChangePasswordPresenter(
    private val userProfileInteractor: UserProfileInteractor
) : BasePresenterImpl<ProfileChangePasswordView>() {

    var userProfileFullInfo: UserProfileFullInfo? = null

    private var currentPassword: String = ""
    private var newPassword: String = ""
    private var newPasswordConfirm: String = ""
    private var isVisibleNewPassword: Boolean = false
    private var isVisibleNewPasswordConfirm: Boolean = false
    private var isVisibleCurrentPasswordConfirm: Boolean = false

    fun setProfileFullInfo(profileFullInfo: UserProfileFullInfo) {
        this.userProfileFullInfo = profileFullInfo
        viewState.setMainInformation(profileFullInfo)
    }

    fun setNewPassword(newPassword: String) {
        this.newPassword = newPassword
        checkAvailableChange()
    }

    fun onNewPasswordVisibilitySelect() {
        isVisibleNewPassword = !isVisibleNewPassword
        viewState.setVisibilityNewPassword(isVisibleNewPassword)
    }

    fun onNewPasswordConfirmVisibilitySelect() {
        isVisibleNewPasswordConfirm = !isVisibleNewPasswordConfirm
        viewState.setVisibilityNewPasswordConfirm(isVisibleNewPasswordConfirm)
    }

    fun onCurrentPasswordVisibilitySelect() {
        isVisibleCurrentPasswordConfirm = !isVisibleCurrentPasswordConfirm
        viewState.setVisibilityCurrentPasswordConfirm(isVisibleCurrentPasswordConfirm)
    }

    fun setNewPasswordConfirm(newPasswordConfirm: String) {
        this.newPasswordConfirm = newPasswordConfirm
        checkAvailableChange()
    }

    fun setCurrentPassword(currentPassword: String) {
        this.currentPassword = currentPassword
        checkAvailableChange()
    }

    private fun checkAvailableChange() {
        if (newPasswordConfirm != newPassword || newPasswordConfirm.isEmpty() || newPassword.isEmpty() || currentPassword.isEmpty()) {
            viewState.setUpdateButtonStatus(PasswordButtonStatus.DISABLED)
        } else {
            viewState.setUpdateButtonStatus(PasswordButtonStatus.ENABLED)
        }
    }

    fun selectChangePassword() {
        if (newPasswordConfirm == newPassword && newPasswordConfirm.isNotNullOrEmpty() && newPassword.isNotNullOrEmpty() && currentPassword.isNotNullOrEmpty()) {
            userProfileInteractor.updatePassword(
                newPassword = newPassword,
                currentPassword = currentPassword
            ).subscribe({
                viewState.showPasswordUpdateToast()
            }, {
                viewState.showPasswordUpdateFailToast()
            })
        }
    }

}
