package uddug.com.naukoteka.presentation.profile

import uddug.com.domain.interactors.user_profile.UserProfileInteractor
import uddug.com.naukoteka.global.base.BasePresenterImpl
import moxy.InjectViewState
import toothpick.InjectConstructor
import uddug.com.domain.entities.profile.UserProfileFullInfo

@InjectConstructor
@InjectViewState
class ProfileHelpSupportPresenter(
    private val userProfileInteractor: UserProfileInteractor
) : BasePresenterImpl<HelpSupportView>() {

    private var profileFullInfo: UserProfileFullInfo? = null

    fun setProfileFullInfo(profileFullInfo: UserProfileFullInfo) {
        this.profileFullInfo = profileFullInfo
    }


}
