package uddug.com.naukoteka.ui.fragments.profile.settings

import uddug.com.domain.interactors.user_profile.UserProfileInteractor
import uddug.com.naukoteka.global.base.BasePresenterImpl
import moxy.InjectViewState
import toothpick.InjectConstructor
import uddug.com.domain.entities.profile.UserProfileFullInfo

@InjectConstructor
@InjectViewState
class ProfileAppSettingsPresenter(
    private val userProfileInteractor: UserProfileInteractor
) : BasePresenterImpl<ProfileAppSettingsView>() {

    companion object {
        private const val errorTag = "ProfileEditPresenterError"
    }

    var userProfileFullInfo: UserProfileFullInfo? = null


}
