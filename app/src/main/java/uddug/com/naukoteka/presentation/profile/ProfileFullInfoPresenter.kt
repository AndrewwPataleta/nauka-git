package uddug.com.naukoteka.presentation.profile

import uddug.com.domain.interactors.user_profile.UserProfileInteractor
import uddug.com.naukoteka.global.base.BasePresenterImpl
import moxy.InjectViewState
import toothpick.InjectConstructor
import uddug.com.domain.entities.profile.UserProfileFullInfo

@InjectConstructor
@InjectViewState
class ProfileFullInfoPresenter(
    private val userProfileInteractor: UserProfileInteractor
) : BasePresenterImpl<ProfileFullView>() {

    private var profileFullInfo: UserProfileFullInfo? = null

    fun setProfileFullInfo(profileFullInfo: UserProfileFullInfo) {
        this.profileFullInfo = profileFullInfo
        viewState.setProfileFullInfo(profileFullInfo)
    }

}
