package uddug.com.naukoteka.presentation.profile.edit

import android.annotation.SuppressLint
import android.util.Log
import uddug.com.domain.interactors.user_profile.UserProfileInteractor
import uddug.com.naukoteka.global.base.BasePresenterImpl
import moxy.InjectViewState
import toothpick.InjectConstructor
import uddug.com.domain.entities.profile.UserProfileFullInfo

@InjectConstructor
@InjectViewState
class ProfileEditPresenter(
    private val userProfileInteractor: UserProfileInteractor
) : BasePresenterImpl<ProfileEditView>() {

    companion object {
        private const val errorTag = "ProfileEditPresenterError"
    }

    var userProfileFullInfo: UserProfileFullInfo? = null

    fun setProfileFullInfo(profileFullInfo: UserProfileFullInfo) {
        this.userProfileFullInfo = profileFullInfo
        viewState.setMainInformation(profileFullInfo)
    }

    fun selectUpdateProfileImage() {
        userProfileFullInfo?.let { viewState.openProfileEditPhotoDialog(it) }
    }

    fun selectUpdateProfileHeader() {
        userProfileFullInfo?.let { viewState.openProfileHeaderEditPhotoDialog(it) }
    }

    fun selectEditProfileId() {
        userProfileFullInfo?.let {
            viewState.openEditProfileId(it)
        }
    }

    fun askForDeleteBanner() {
        viewState.askForDeleteBanner()
    }

    @SuppressLint("CheckResult")
    fun confirmDeleteBanner() {
        userProfileFullInfo?.id?.let {
            userProfileInteractor.deleteUserBanner(userId = it).subscribe({
                loadProfile()
            }, {
                Log.e(errorTag, it.message.toString())
            })
        }
    }

    fun askForOpenUserData() {
        userProfileFullInfo?.let { viewState.openProfileEditPersonalData(it) }
    }

    fun askForOpenPersonalIds() {
        userProfileFullInfo?.let { viewState.openProfileEditPersonalIdsData(it) }
    }


    fun askForOpenAcademicProfile() {
        userProfileFullInfo?.let { viewState.openProfileAcademicDegreeEdit(it) }
    }

    fun askForOpenEducationInfo() {
        userProfileFullInfo?.let { viewState.openProfileEducationInfo(it) }
    }

    fun askForOpenCarrierInfo() {
        userProfileFullInfo?.let { viewState.openProfileCarrierInfo(it) }
    }

    fun askForOpenAddressesList() {
        userProfileFullInfo?.let { viewState.openProfileAddressesEdit(it) }
    }

    fun askForOpenContactsEdit() {
        userProfileFullInfo?.let { viewState.openProfileContactsEdit(it) }
    }

    fun askForOpenSettings() {
        userProfileFullInfo?.let { viewState.openProfileSettings(it) }
    }

    @SuppressLint("CheckResult")
    fun loadProfile() {
        userProfileInteractor.getUserProfilePreviewInfo().subscribe({
            userProfileFullInfo = it
            viewState.setMainInformation(it)
        }, {
        })
    }
}
