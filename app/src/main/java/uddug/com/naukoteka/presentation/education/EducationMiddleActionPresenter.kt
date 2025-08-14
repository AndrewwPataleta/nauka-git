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
class EducationMiddleActionPresenter(
    private val userProfileInteractor: UserProfileInteractor,
    private val locationInteractor: LocationInteractor,
) : BasePresenterImpl<EducationMiddleActionView>() {

    private var screenActionType: ScreenActionType = ScreenActionType.CREATE

    private var currentEducationId: String? = null

    var compositeDisposable: CompositeDisposable = CompositeDisposable()

    var userProfileFullInfo: UserProfileFullInfo? = null

    private var currentEducation: Education = Education()

    val calendar: GregorianCalendar = GregorianCalendar()

    private var lastSettlements: List<Settlement> = emptyList()

    companion object {
        private const val middleCType = "53:5"
        private const val highCType = "53:6"
        private const val additionalCType = "53:4"
    }

    fun setCurrentEducationId(educationId: String) {
        currentEducationId = educationId
        currentEducation.id = currentEducationId
        screenActionType = ScreenActionType.EDIT
        setEducationInfo()
    }

    private fun setEducationInfo() {
        userProfileFullInfo?.education?.find {
            it.id == currentEducationId
        }?.let { education ->
            currentEducation = education
            viewState.setCurrentEducationInfo(education)
        }
    }

    fun setProfileFullInfo(profileFullInfo: UserProfileFullInfo) {
        this.userProfileFullInfo = profileFullInfo

    }

    fun askForOpenCountrySelect() {
        viewState.openCountrySelectPage(
            currentEducation?.country?.id
        )
    }

    fun selectUpdateEducation() {
        when (screenActionType) {
            ScreenActionType.CREATE -> {
                if (currentEducation.country?.id.isNullOrEmpty()) {
                    viewState.showCreateValidationError()
                } else {
                    currentEducation?.let {
                        currentEducation.cLevel = middleCType
                        userProfileInteractor.createUserEducation(
                            userId = userProfileFullInfo?.id.orEmpty(),
                            education = listOf(it),
                        ).subscribe({
                            viewState.educationSuccessUpdated()
                        }, {})
                    }?.let {
                        compositeDisposable.add(
                            it
                        )
                    }
                }
            }

            ScreenActionType.EDIT -> {
                if (currentEducation?.country?.id.isNullOrEmpty()) {
                    viewState.showUpdateValidationError()
                } else {
                    currentEducation?.let {
                        userProfileInteractor.updateUserEducation(
                            userId = userProfileFullInfo?.id.orEmpty(),
                            education = it,
                        ).subscribe({
                            viewState.educationSuccessUpdated()
                        }, {})
                    }?.let {
                        compositeDisposable.add(
                            it
                        )
                    }
                }
            }
        }
    }

    fun setEducationSettlement(settlement: String) {
        currentEducation.city = settlement
        if (settlement.isNotNullOrEmpty()) {
            currentEducation.country?.id?.let {
                compositeDisposable.add(
                    locationInteractor.findSettlementsByCountry(
                        countryId = it,
                        query = settlement,
                    ).subscribe({
                        lastSettlements = it
                        viewState.setSettlements(it)
                    }, {

                    })
                )
            }
        }
    }

    fun setSchool(school: String) {
        currentEducation.name = school
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
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
        currentEducation.let { viewState.setCurrentEducationInfo(it) }
    }

    enum class ScreenActionType {
        CREATE,
        EDIT
    }

}
