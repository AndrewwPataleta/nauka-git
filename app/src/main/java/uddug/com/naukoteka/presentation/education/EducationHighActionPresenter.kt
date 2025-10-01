package uddug.com.naukoteka.presentation.education

import io.reactivex.disposables.CompositeDisposable
import moxy.InjectViewState
import toothpick.InjectConstructor
import uddug.com.domain.entities.country.Country
import uddug.com.domain.entities.country.Settlement
import uddug.com.domain.entities.profile.Education
import uddug.com.domain.entities.profile.UserProfileFullInfo
import uddug.com.domain.interactors.country.LocationInteractor
import uddug.com.domain.interactors.user_profile.UserProfileInteractor
import uddug.com.naukoteka.global.base.BasePresenterImpl
import uddug.com.naukoteka.utils.text.isNotNullOrEmpty
import java.util.Calendar
import java.util.GregorianCalendar

@InjectConstructor
@InjectViewState
class EducationHighActionPresenter(
    private val userProfileInteractor: UserProfileInteractor,
    private val locationInteractor: LocationInteractor,
) : BasePresenterImpl<EducationHighActionView>() {

    private var screenActionType: ScreenActionType = ScreenActionType.CREATE
    private var currentEducationId: String? = null
    private val compositeDisposable = CompositeDisposable()
    var userProfileFullInfo: UserProfileFullInfo? = null
    private var currentEducation: Education = Education()
    private val calendar = GregorianCalendar()
    private var lastSettlements: List<Settlement> = emptyList()

    companion object {
        private const val middleCType = "53:5"
        private const val highCType = "53:6"
        private const val additionalCType = "53:4"
    }

    fun setCurrentEducationId(educationId: String) {
        currentEducationId = educationId
        currentEducation.id = educationId
        screenActionType = ScreenActionType.EDIT
        setEducationInfo()
    }

    private fun setEducationInfo() {
        userProfileFullInfo?.education?.find { it.id == currentEducationId }?.let { education ->
            currentEducation = education
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
        if (currentEducation.country?.id.isNullOrEmpty() || currentEducation.specialty.isNullOrEmpty()) {
            when (screenActionType) {
                ScreenActionType.CREATE -> viewState.showCreateValidationError()
                ScreenActionType.EDIT -> viewState.showUpdateValidationError()
            }
            return
        }

        val disposable = when (screenActionType) {
            ScreenActionType.CREATE -> {
                currentEducation.cLevel = highCType
                userProfileInteractor.createUserEducation(
                    userId = userProfileFullInfo?.id.orEmpty(),
                    education = listOf(currentEducation)
                )
            }
            ScreenActionType.EDIT -> {
                userProfileInteractor.updateUserEducation(
                    userId = userProfileFullInfo?.id.orEmpty(),
                    education = currentEducation
                )
            }
        }.subscribe({
            viewState.educationSuccessUpdated()
        }, {
            
        })

        compositeDisposable.add(disposable)
    }

    fun setEducationSettlement(settlement: String) {
        currentEducation.city = settlement
        if (settlement.isNotNullOrEmpty() && !currentEducation.country?.id.isNullOrEmpty()) {
            currentEducation.country!!.id?.let {
                locationInteractor.findSettlementsByCountry(
                    countryId = it,
                    query = settlement
                ).subscribe({ settlements ->
                    lastSettlements = settlements
                    viewState.setSettlements(settlements)
                }, {
                    
                })
            }?.let {
                compositeDisposable.add(
                    it
                )
            }
        }
    }

    fun setSchool(school: String) {
        currentEducation.name = school
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

    fun setSpeciality(specialty: String) {
        currentEducation.specialty = specialty
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }

    enum class ScreenActionType {
        CREATE,
        EDIT
    }
}
