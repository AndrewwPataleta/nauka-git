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
        private const val errorTag = "ProfileEditPlacementViewError"
        private const val middleCType = "53:5"
        private const val middleSecondCType = "53:4"
        private const val highCType = "53:6"
        private const val highCTypeSecond = "53:7"
        private const val additionalCType = "53:4"

    }

    var compositeDisposable: CompositeDisposable = CompositeDisposable()

    var userProfileFullInfo: UserProfileFullInfo? = null

    var educationScreenType: EducationScreenType? = null

    fun setEducationType(educationScreenType: String) {
        this.educationScreenType = when (educationScreenType) {
            EducationScreenType.MIDDLE.name -> {
                EducationScreenType.MIDDLE
            }

            EducationScreenType.HIGH.name -> {
                EducationScreenType.HIGH
            }

            EducationScreenType.ADDITIONAL.name -> {
                EducationScreenType.ADDITIONAL
            }

            else -> EducationScreenType.MIDDLE
        }

    }

    fun setProfileFullInfo(profileFullInfo: UserProfileFullInfo) {
        this.userProfileFullInfo = profileFullInfo
        when (educationScreenType) {
            EducationScreenType.MIDDLE -> {
                viewState.setEducationItems(profileFullInfo.education.filter {
                    it.cLevel == middleCType || it.cLevel == middleSecondCType
                })
            }

            EducationScreenType.HIGH -> {
                viewState.setEducationItems(profileFullInfo.education.filter {
                    it.cLevel == highCType || it.cLevel == highCTypeSecond
                })
            }

            EducationScreenType.ADDITIONAL -> {
                viewState.setEducationItems(profileFullInfo.education.filter {
                    it.cLevel == null
                })
            }

            null -> {
                viewState.setEducationItems(profileFullInfo.education)
            }
        }

    }

    fun askForDeleteItem(education: Education) {
        viewState.showDeleteDialog(
            education
        )
    }

    fun askForDetailInfoItem(education: Education) {
        userProfileFullInfo?.let {
            educationScreenType?.let { it1 ->
                viewState.showDetailScreen(
                    it, education.id, type = it1
                )
            }
        }
    }

    fun askForAddNewEducation() {
        userProfileFullInfo?.let { user ->
            educationScreenType?.let { educationScreenType ->
                viewState.showAddNewEducation(
                    profileInfo = user,
                    type = educationScreenType
                )
            }

        }
    }

    fun confirmDeleteEducation(education: Education) {
        userProfileFullInfo?.id?.let {
            userProfileInteractor.removeUserEducation(
                userId = it, education
            ).subscribe({
                userProfileFullInfo = userProfileFullInfo?.copy(
                    education = userProfileFullInfo?.education?.filter {
                        it.id != education.id
                    } ?: emptyList()
                )
                userProfileFullInfo?.let { viewState.setEducationItems(it.education) }
            }, {
            })
        }?.let {
            compositeDisposable.add(
                it
            )
        }

    }

    fun loadProfile() {
        compositeDisposable.add(
            userProfileInteractor.getUserProfilePreviewInfo().subscribe({
                userProfileFullInfo = it
                setProfileFullInfo(it)
            }, {
            })
        )
    }


    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
    }


}
