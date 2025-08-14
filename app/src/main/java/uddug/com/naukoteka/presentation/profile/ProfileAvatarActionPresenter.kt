package uddug.com.naukoteka.presentation.profile

import uddug.com.domain.interactors.user_profile.UserProfileInteractor
import uddug.com.naukoteka.global.base.BasePresenterImpl
import moxy.InjectViewState
import toothpick.InjectConstructor
import uddug.com.domain.entities.profile.UserProfileFullInfo
import uddug.com.naukoteka.utils.text.isNotNullOrEmpty
import java.io.File

@InjectConstructor
@InjectViewState
class ProfileAvatarActionPresenter(
    private val userProfileInteractor: UserProfileInteractor
) : BasePresenterImpl<ProfilePhotoActionView>() {

    private var profileFullInfo: UserProfileFullInfo? = null
    private var currentChangeType: IMAGE_TYPE_AVATAR? = null

    fun setProfileFullInfo(profileFullInfo: UserProfileFullInfo) {
        this.profileFullInfo = profileFullInfo
        viewState.setOpenPhotoAvailable(
            available = profileFullInfo.image?.path.isNotNullOrEmpty()
        )
    }

    fun setCurrentChangeType(changeType: String) {
        when (changeType) {
            IMAGE_TYPE_AVATAR.AVATAR.type -> currentChangeType = IMAGE_TYPE_AVATAR.AVATAR
            IMAGE_TYPE_AVATAR.BANNER.type -> currentChangeType = IMAGE_TYPE_AVATAR.BANNER
        }
    }

    fun selectShowProfileImage() {
        when (currentChangeType) {
            IMAGE_TYPE_AVATAR.AVATAR -> {
                profileFullInfo?.let { viewState.openProfilePhotoImageView(it) }
            }
            IMAGE_TYPE_AVATAR.BANNER -> {
                profileFullInfo?.let { viewState.openProfileBannerImageView(it) }
            }
            null -> {


            }
        }

    }

    fun chooseDeletePhoto() {
        viewState.showDeletePhotoDialog()
    }

    fun uploadUserImage(
        file: File
    ) {
        profileFullInfo?.id?.let {
            when (currentChangeType) {
                IMAGE_TYPE_AVATAR.AVATAR -> {
                    userProfileInteractor.uploadUserAvatar(
                        userId = it,
                        avatar = file
                    ).subscribe({
                        viewState.successfulUpload()
                    }, {})
                }
                IMAGE_TYPE_AVATAR.BANNER -> {
                    userProfileInteractor.uploadUserBanner(
                        userId = it,
                        avatar = file
                    ).subscribe({
                        viewState.successfulUpload()
                    }, {})
                }
                null -> Unit
            }

        }
    }


    fun confirmDeletePhoto() {
        profileFullInfo?.id?.let {
            userProfileInteractor.deleteUserAvatar(
                userId = it
            ).subscribe({
                viewState.successfulDeleteAvatar()
            }, {})
        }
    }

    enum class IMAGE_TYPE_AVATAR(val type: String) {
        AVATAR("avatar"),
        BANNER("banner")
    }

}
