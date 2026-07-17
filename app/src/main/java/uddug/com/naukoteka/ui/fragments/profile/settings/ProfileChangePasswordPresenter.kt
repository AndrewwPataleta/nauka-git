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
    private var isNewPasswordVisible: Boolean = false
    private var isNewPasswordConfirmVisible: Boolean = false
    private var isCurrentPasswordVisible: Boolean = false

    fun setProfileFullInfo(profileFullInfo: UserProfileFullInfo) {
        userProfileFullInfo = profileFullInfo
        viewState.setMainInformation(profileFullInfo)
    }

    fun setNewPassword(password: String) {
        newPassword = password
        refreshButtonState()
    }

    fun setNewPasswordConfirm(password: String) {
        newPasswordConfirm = password
        refreshButtonState()
    }

    fun setCurrentPassword(password: String) {
        currentPassword = password
        refreshButtonState()
    }

    fun onNewPasswordVisibilitySelect() {
        isNewPasswordVisible = !isNewPasswordVisible
        viewState.setVisibilityNewPassword(isNewPasswordVisible)
    }

    fun onNewPasswordConfirmVisibilitySelect() {
        isNewPasswordConfirmVisible = !isNewPasswordConfirmVisible
        viewState.setVisibilityNewPasswordConfirm(isNewPasswordConfirmVisible)
    }

    fun onCurrentPasswordVisibilitySelect() {
        isCurrentPasswordVisible = !isCurrentPasswordVisible
        viewState.setVisibilityCurrentPasswordConfirm(isCurrentPasswordVisible)
    }

    fun selectChangePassword() {
        if (!isPasswordChangeValid()) return
        userProfileInteractor.updatePassword(
            newPassword = newPassword,
            currentPassword = currentPassword
        ).subscribe({
            viewState.showPasswordUpdateToast()
        }, {
            viewState.showPasswordUpdateFailToast()
        })
    }

    private fun refreshButtonState() {
        val status = if (isPasswordChangeValid()) {
            PasswordButtonStatus.ENABLED
        } else {
            PasswordButtonStatus.DISABLED
        }
        viewState.setUpdateButtonStatus(status)
    }

    private fun isPasswordChangeValid(): Boolean =
        newPassword.isNotNullOrEmpty() &&
        newPasswordConfirm.isNotNullOrEmpty() &&
        currentPassword.isNotNullOrEmpty() &&
        newPassword == newPasswordConfirm
}
