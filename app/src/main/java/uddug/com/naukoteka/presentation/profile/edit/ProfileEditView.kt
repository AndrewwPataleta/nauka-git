package uddug.com.naukoteka.presentation.profile.edit

import uddug.com.naukoteka.global.views.InformativeView
import uddug.com.naukoteka.global.views.LoadingView
import moxy.MvpView
import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType
import uddug.com.domain.entities.profile.UserProfileFullInfo

@StateStrategyType(OneExecutionStateStrategy::class)
interface ProfileEditView : MvpView, LoadingView, InformativeView {
    fun setMainInformation(profileInfo: UserProfileFullInfo)
    fun openProfileEditPhotoDialog(profileInfo: UserProfileFullInfo)
    fun openProfileHeaderEditPhotoDialog(profileInfo: UserProfileFullInfo)
    fun openProfileEditPersonalData(profileInfo: UserProfileFullInfo)
    fun openProfileEditPersonalIdsData(profileInfo: UserProfileFullInfo)
    fun openProfileAcademicDegreeEdit(profileInfo: UserProfileFullInfo)
    fun openProfileEducationInfo(
        profileInfo: UserProfileFullInfo,

        )

    fun openProfileCarrierInfo(
        profileInfo: UserProfileFullInfo,

        )

    fun openProfileAddressesEdit(
        profileInfo: UserProfileFullInfo,
    )

    fun openProfileContactsEdit(
        profileInfo: UserProfileFullInfo,
    )

    fun openProfileSettings(
        profileInfo: UserProfileFullInfo,
    )

    fun openEditProfileId(profileInfo: UserProfileFullInfo)
    fun askForDeleteBanner()
}
