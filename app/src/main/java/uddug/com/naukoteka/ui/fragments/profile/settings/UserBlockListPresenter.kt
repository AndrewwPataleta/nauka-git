package uddug.com.naukoteka.ui.fragments.profile.settings

import moxy.InjectViewState
import toothpick.InjectConstructor
import uddug.com.domain.entities.profile.UserProfileFullInfo
import uddug.com.domain.interactors.country.LocationInteractor
import uddug.com.naukoteka.global.base.BasePresenterImpl


@InjectConstructor
@InjectViewState
class UserBlockListPresenter(
    private val locationInteractor: LocationInteractor,
) : BasePresenterImpl<UserBlockListView>() {

    var userProfileFullInfo: UserProfileFullInfo? = null

    fun setProfileFullInfo(profileFullInfo: UserProfileFullInfo) {
        userProfileFullInfo = profileFullInfo
    }
}
