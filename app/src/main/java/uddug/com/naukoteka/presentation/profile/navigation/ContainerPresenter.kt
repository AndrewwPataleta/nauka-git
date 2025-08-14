package uddug.com.naukoteka.presentation.profile.navigation

import uddug.com.domain.interactors.user_profile.UserProfileInteractor
import uddug.com.naukoteka.global.base.BasePresenterImpl
import toothpick.InjectConstructor
import uddug.com.domain.entities.profile.UserProfileFullInfo
import uddug.com.naukoteka.presentation.profile.ProfileAvatarActionPresenter.IMAGE_TYPE_AVATAR
import java.io.File

@InjectConstructor
class ContainerPresenter(
    private val userProfileInteractor: UserProfileInteractor
) : BasePresenterImpl<ContainerView>() {

    companion object {}

    private var profileFullInfo: UserProfileFullInfo? = null

    fun selectOpenEditFragment(profileFullInfo: UserProfileFullInfo) {
        viewState.openEditFragment(profileFullInfo)
    }

    fun uploadUserImage(
        imageType: IMAGE_TYPE_AVATAR,
        file: File
    ) {
        profileFullInfo?.id?.let {
            userProfileInteractor.uploadUserAvatar(
                userId = it,
                avatar = file
            ).subscribe({

            }, {})
        }
    }
}
