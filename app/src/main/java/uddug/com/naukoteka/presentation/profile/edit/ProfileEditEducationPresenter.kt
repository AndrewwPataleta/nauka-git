package uddug.com.naukoteka.presentation.profile.edit

import io.reactivex.disposables.CompositeDisposable
import moxy.InjectViewState
import toothpick.InjectConstructor
import uddug.com.domain.entities.profile.Education
import uddug.com.domain.entities.profile.UserProfileFullInfo
import uddug.com.domain.interactors.user_profile.UserProfileInteractor
import uddug.com.naukoteka.global.base.BasePresenterImpl

@InjectConstructor
@InjectViewState
class ProfileEditEducationPresenter(
    private val userProfileInteractor: UserProfileInteractor
) : BasePresenterImpl<ProfileMiddleActionEducationView>() {

    companion object {
        private const val middleCType = "53:5"
        private const val middleSecondCType = "53:4"
        private const val highCType = "53:6"
        private const val highCTypeSecond = "53:7"
    }

    private val compositeDisposable = CompositeDisposable()

    var userProfileFullInfo: UserProfileFullInfo? = null
    var educationScreenType: EducationScreenType? = null

    fun setEducationType(educationScreenType: String) {
        this.educationScreenType = when (educationScreenType) {
            EducationScreenType.MIDDLE.name -> EducationScreenType.MIDDLE
            EducationScreenType.HIGH.name -> EducationScreenType.HIGH
            EducationScreenType.ADDITIONAL.name -> EducationScreenType.ADDITIONAL
            else -> EducationScreenType.MIDDLE
        }
    }

    fun setProfileFullInfo(profileFullInfo: UserProfileFullInfo) {
        this.userProfileFullInfo = profileFullInfo
        val filtered = when (educationScreenType) {
            EducationScreenType.MIDDLE -> profileFullInfo.education.filter {
                it.cLevel == middleCType || it.cLevel == middleSecondCType
            }
            EducationScreenType.HIGH -> profileFullInfo.education.filter {
                it.cLevel == highCType || it.cLevel == highCTypeSecond
            }
            EducationScreenType.ADDITIONAL -> profileFullInfo.education.filter { it.cLevel == null }
            null -> profileFullInfo.education
        }
        viewState.setEducationItems(filtered)
    }

    fun askForDeleteItem(education: Education) {
        viewState.showDeleteDialog(education)
    }

    fun askForDetailInfoItem(education: Education) {
        val profile = userProfileFullInfo ?: return
        val type = educationScreenType ?: return
        viewState.showDetailScreen(profile, education.id, type)
    }

    fun askForAddNewEducation() {
        val profile = userProfileFullInfo ?: return
        val type = educationScreenType ?: return
        viewState.showAddNewEducation(profileInfo = profile, type = type)
    }

    fun confirmDeleteEducation(education: Education) {
        userProfileFullInfo?.id?.let { userId ->
            compositeDisposable.add(
                userProfileInteractor.removeUserEducation(userId = userId, education).subscribe({
                    userProfileFullInfo = userProfileFullInfo?.copy(
                        education = userProfileFullInfo?.education?.filter { it.id != education.id } ?: emptyList()
                    )
                    userProfileFullInfo?.let { viewState.setEducationItems(it.education) }
                }, {})
            )
        }
    }

    fun loadProfile() {
        compositeDisposable.add(
            userProfileInteractor.getUserProfilePreviewInfo().subscribe({ profile ->
                userProfileFullInfo = profile
                setProfileFullInfo(profile)
            }, {})
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }
}
