package uddug.com.naukoteka.presentation.education

import io.reactivex.disposables.CompositeDisposable
import moxy.InjectViewState
import toothpick.InjectConstructor
import uddug.com.domain.entities.country.Country
import uddug.com.domain.entities.profile.Education
import uddug.com.domain.entities.profile.UserProfileFullInfo
import uddug.com.domain.interactors.country.LocationInteractor
import uddug.com.domain.interactors.user_profile.UserProfileInteractor
import uddug.com.naukoteka.global.base.BasePresenterImpl
import java.util.Calendar
import java.util.GregorianCalendar

@InjectConstructor
@InjectViewState
class EducationAdditionalActionPresenter(
    private val userProfileInteractor: UserProfileInteractor,
    private val locationInteractor: LocationInteractor,
) : BasePresenterImpl<EducationAdditionalActionView>() {

    private var screenActionType: ScreenActionType = ScreenActionType.CREATE
    private var currentEducationId: String? = null
    private val compositeDisposable = CompositeDisposable()
    private var currentEducation: Education = Education()
    private val calendar = GregorianCalendar()

    var userProfileFullInfo: UserProfileFullInfo? = null

    companion object {
        private const val additionalCType = "53:4"
    }

    fun setCurrentEducationId(educationId: String) {
        currentEducationId = educationId
        currentEducation.id = educationId
        screenActionType = ScreenActionType.EDIT
        loadEducationInfo()
    }

    private fun loadEducationInfo() {
        userProfileFullInfo?.education?.find { it.id == currentEducationId }?.let { education ->
            currentEducation = education
            currentEducation.cLevel = additionalCType
            viewState.setCurrentEducationInfo(education)
        }
    }

    fun setProfileFullInfo(profileFullInfo: UserProfileFullInfo) {
        this.userProfileFullInfo = profileFullInfo
    }

    fun askForOpenCountrySelect() {
        viewState.openCountrySelectPage(currentEducation.country?.id)
    }

    fun selectUpdateEducation() {
        when (screenActionType) {
            ScreenActionType.CREATE -> {
                currentEducation.cLevel = additionalCType
                compositeDisposable.add(
                    userProfileInteractor.createUserEducation(
                        userId = userProfileFullInfo?.id.orEmpty(),
                        education = listOf(currentEducation),
                    ).subscribe({ viewState.educationSuccessUpdated() }, {})
                )
            }
            ScreenActionType.EDIT -> {
                compositeDisposable.add(
                    userProfileInteractor.updateUserEducation(
                        userId = userProfileFullInfo?.id.orEmpty(),
                        education = currentEducation,
                    ).subscribe({ viewState.educationSuccessUpdated() }, {})
                )
            }
        }
    }

    fun setEducationSettlement(settlement: String) {
        currentEducation.city = settlement
    }

    fun setSchool(school: String) {
        currentEducation.name = school
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }

    fun setEndYear(year: String) {
        calendar.set(year.toInt(), Calendar.JULY, 31)
        currentEducation.endDate = calendar.toZonedDateTime().toLocalDate().toString()
    }

    fun setStartYear(year: String) {
        calendar.set(year.toInt(), Calendar.JULY, 31)
        currentEducation.startDate = calendar.toZonedDateTime().toLocalDate().toString()
    }

    fun setSelectedCountry(country: Country) {
        currentEducation.country = country
        viewState.setCurrentEducationInfo(currentEducation)
    }

    enum class ScreenActionType {
        CREATE,
        EDIT
    }
}
