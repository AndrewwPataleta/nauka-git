package uddug.com.naukoteka.ui.fragments.profile.settings

import uddug.com.domain.entities.profile.UserProfileFullInfo
import uddug.com.domain.interactors.user_profile.UserProfileInteractor
import uddug.com.naukoteka.global.base.BasePresenterImpl
import moxy.InjectViewState
import toothpick.InjectConstructor

@InjectConstructor
@InjectViewState
class ProfileAppSettingsPresenter(
    private val userProfileInteractor: UserProfileInteractor
) : BasePresenterImpl<ProfileAppSettingsView>() {

    var userProfileFullInfo: UserProfileFullInfo? = null
}
