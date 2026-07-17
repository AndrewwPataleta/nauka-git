package uddug.com.naukoteka.presentation.profile.edit

import android.annotation.SuppressLint
import io.reactivex.disposables.CompositeDisposable
import moxy.InjectViewState
import toothpick.InjectConstructor
import uddug.com.domain.entities.profile.UserProfileFullInfo
import uddug.com.domain.interactors.user_profile.UserProfileInteractor
import uddug.com.domain.repositories.models.UserAcademicDegrees
import uddug.com.naukoteka.global.base.BasePresenterImpl
import uddug.com.naukoteka.utils.text.isNotNullOrEmpty
import java.util.Calendar
import java.util.GregorianCalendar

@InjectConstructor
@InjectViewState
class ProfileEditAcademicDegreePresenter(
    private val userProfileInteractor: UserProfileInteractor
) : BasePresenterImpl<ProfileEditAcademicDegreeView>() {

    private val compositeDisposable = CompositeDisposable()
    private val calendar: GregorianCalendar = GregorianCalendar()

    var userProfileFullInfo: UserProfileFullInfo? = null

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
            userProfileFullInfo?.id?.let { userId ->
                val newDegrees = list.filter { it.id == null && it.academicName.isNotNullOrEmpty() && it.academicYear.isNotNullOrEmpty() }
                    .map { academic ->
                        UserAcademicDegrees(
                            name = academic.academicName,
                            titleDate = buildDateString(academic.academicYear)
                        )
                    }
                val updatedDegrees = list.filter { it.id != null && it.academicName.isNotNullOrEmpty() && it.academicYear.isNotNullOrEmpty() }
                    .map { academic ->
                        UserAcademicDegrees(
                            id = academic.id,
                            name = academic.academicName,
                            titleDate = buildDateString(academic.academicYear)
                        )
                    }
                userProfileInteractor.addUserAcademic(userId = userId, degrees = newDegrees)
                    .andThen(userProfileInteractor.updateUserAcademic(userId = userId, degrees = updatedDegrees))
                    .subscribe({
                        refreshProfile()
                    }, {
                        refreshProfile()
                        it.printStackTrace()
                    })
            }
        }
    }

    private fun buildDateString(year: String): String {
        calendar.set(year.toInt(), Calendar.JULY, 31)
        return calendar.toZonedDateTime().toLocalDate().toString()
    }

    private fun refreshProfile() {
        userProfileInteractor.getUserProfilePreviewInfo().subscribe({ profile ->
            userProfileFullInfo = profile
            viewState.setMainInformation(profile)
        }, {})
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }
}
