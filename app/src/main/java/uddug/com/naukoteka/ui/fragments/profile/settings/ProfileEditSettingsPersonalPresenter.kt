package uddug.com.naukoteka.ui.fragments.profile.settings

import moxy.InjectViewState
import toothpick.InjectConstructor
import uddug.com.domain.entities.profile.UserProfileFullInfo
import uddug.com.domain.interactors.user_profile.UserProfileInteractor
import uddug.com.naukoteka.global.base.BasePresenterImpl

@InjectConstructor
@InjectViewState
class ProfileEditSettingsPersonalPresenter(
    private val userProfileInteractor: UserProfileInteractor
) : BasePresenterImpl<ProfileEditSettingsPersonalView>() {

    var userProfileFullInfo: UserProfileFullInfo? = null

    init {
        userProfileInteractor.getUserSettings().subscribe({

        }, {

        })
    }

    fun setProfileFullInfo(profileFullInfo: UserProfileFullInfo) {
        this.userProfileFullInfo = profileFullInfo
        viewState.setMainInformation(profileFullInfo)
    }

    fun askForEditVisibility(visibilityType: VisibilityType) {
        viewState.showVisibilityChangeDialog(visibilityType, VisibilityMode.ALL)
    }

    fun setVisibilityResult(visibilityType: VisibilityType, visibilityMode: VisibilityMode) {
        when (visibilityType) {
            VisibilityType.BLOCK_MAIN_INFO -> {
                viewState.setVisibilitySettings(
                    visibilityType,
                    visibilityMode
                )
            }
        }
    }
}

enum class VisibilityMode {
    ALL,
    SUBS,
    NO_ONE,
}

enum class VisibilityType {
    BLOCK_MAIN_INFO
}
