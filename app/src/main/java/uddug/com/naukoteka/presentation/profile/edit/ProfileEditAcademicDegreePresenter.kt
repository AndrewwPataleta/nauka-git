package uddug.com.naukoteka.presentation.profile.edit

import android.annotation.SuppressLint
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import moxy.InjectViewState
import toothpick.InjectConstructor
import uddug.com.domain.entities.profile.UserProfileFullInfo
import uddug.com.domain.interactors.user_profile.UserProfileInteractor
import uddug.com.domain.repositories.models.UserAcademicDegrees
import uddug.com.naukoteka.global.base.BasePresenterImpl
import uddug.com.naukoteka.utils.text.isNotNullOrEmpty
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.concurrent.TimeUnit


@InjectConstructor
@InjectViewState
class ProfileEditAcademicDegreePresenter(
    private val userProfileInteractor: UserProfileInteractor
) : BasePresenterImpl<ProfileEditAcademicDegreeView>() {

    companion object {
        private const val errorTag = "ProfileEditPlacementViewError"
    }

    var compositeDisposable: CompositeDisposable = CompositeDisposable()

    var userProfileFullInfo: UserProfileFullInfo? = null

    val calendar: GregorianCalendar = GregorianCalendar()

    fun setProfileFullInfo(profileFullInfo: UserProfileFullInfo) {
        this.userProfileFullInfo = profileFullInfo
        viewState.setMainInformation(profileFullInfo)
    }

    fun askToAddNewAcademicDegree() {
        viewState.addNewAcademicDegree()
    }

    @SuppressLint("CheckResult")
    fun askToSaveNewAcademicDegree() {
        viewState.getListDegrees { list ->
            userProfileFullInfo?.id?.let {
                userProfileInteractor.addUserAcademic(
                    userId = it,
                    degrees = list.filter { it.id == null && it.academicName.isNotNullOrEmpty() && it.academicYear.isNotNullOrEmpty() }
                        .map { academic ->
                            UserAcademicDegrees(
                                name = academic.academicName,
                                titleDate = let {
                                    calendar.set(academic.academicYear.toInt(), Calendar.JULY, 31)
                                    calendar.toZonedDateTime().toLocalDate().toString()
                                }
                            )
                        }
                ).andThen(
                    userProfileInteractor.updateUserAcademic(
                        userId = it,
                        degrees = list.filter { it.id != null && it.academicName.isNotNullOrEmpty() && it.academicYear.isNotNullOrEmpty() }
                            .map { academic ->
                                UserAcademicDegrees(
                                    id = academic.id,
                                    name = academic.academicName,
                                    titleDate = let {
                                        calendar.set(
                                            academic.academicYear.toInt(),
                                            Calendar.JULY,
                                            31
                                        )
                                        calendar.toZonedDateTime().toLocalDate().toString()
                                    }
                                )
                            }
                    )
                ).subscribe({
                    updateUserProfile()
                }, {
                    updateUserProfile()
                    it.printStackTrace()
                })
            }
        }
    }

    fun updateUserProfile() {
        userProfileInteractor.getUserProfilePreviewInfo().subscribe({
            userProfileFullInfo = it
            viewState.setMainInformation(it)
        }, {

        })
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
    }


}
