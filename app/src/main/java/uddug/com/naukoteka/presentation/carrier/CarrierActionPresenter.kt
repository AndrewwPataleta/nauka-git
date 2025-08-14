package uddug.com.naukoteka.presentation.carrier

import io.reactivex.disposables.CompositeDisposable
import moxy.InjectViewState
import toothpick.InjectConstructor
import uddug.com.domain.entities.country.Country
import uddug.com.domain.entities.country.Settlement
import uddug.com.domain.entities.profile.LaborActivities
import uddug.com.domain.entities.profile.UserProfileFullInfo
import uddug.com.domain.interactors.country.LocationInteractor
import uddug.com.domain.interactors.user_profile.UserProfileInteractor
import uddug.com.naukoteka.global.base.BasePresenterImpl
import uddug.com.naukoteka.utils.text.isNotNullOrEmpty
import java.util.Calendar
import java.util.GregorianCalendar

@InjectConstructor
@InjectViewState
class CarrierActionPresenter(
    private val userProfileInteractor: UserProfileInteractor,
    private val locationInteractor: LocationInteractor,
) : BasePresenterImpl<CarrierActionView>() {

    private var screenActionType: ScreenActionType = ScreenActionType.CREATE

    private var currentcarrierId: String? = null

    var compositeDisposable: CompositeDisposable = CompositeDisposable()

    var userProfileFullInfo: UserProfileFullInfo? = null

    private var currentcarrier: LaborActivities = LaborActivities()

    val calendar: GregorianCalendar = GregorianCalendar()

    private var lastSettlements: List<Settlement> = emptyList()

    companion object {
        private const val middleCType = "53:5"
        private const val highCType = "53:6"
        private const val additionalCType = "53:4"
    }

    fun setCurrentcarrierId(carrierId: String) {
        currentcarrierId = carrierId
        currentcarrier.id = currentcarrierId
        screenActionType = ScreenActionType.EDIT
        setcarrierInfo()
    }

    private fun setcarrierInfo() {
        userProfileFullInfo?.laborActivity?.find {
            it.id == currentcarrierId
        }?.let { carrier ->
            currentcarrier = carrier
            viewState.setCurrentCarrierInfo(carrier)
        }
    }

    fun setProfileFullInfo(profileFullInfo: UserProfileFullInfo) {
        this.userProfileFullInfo = profileFullInfo

    }

    fun askForOpenCountrySelect() {
        viewState.openCountrySelectPage(
            currentcarrier?.country?.id
        )
    }

    fun selectUpdatecarrier() {
        when (screenActionType) {
            ScreenActionType.CREATE -> {
                if (currentcarrier.country?.id.isNullOrEmpty()) {
                    viewState.showCreateValidationError()
                } else {
                    currentcarrier?.let {
                        userProfileInteractor.createUserLabor(
                            userId = userProfileFullInfo?.id.orEmpty(),
                            labor = it,
                        ).subscribe({
                            viewState.carrierSuccessUpdated()
                        }, {})
                    }?.let {
                        compositeDisposable.add(
                            it
                        )
                    }
                }
            }

            ScreenActionType.EDIT -> {
                if (currentcarrier?.country?.id.isNullOrEmpty()) {
                    viewState.showUpdateValidationError()
                } else {
                    currentcarrier?.let {
                        userProfileInteractor.updateUserCarrier(
                            userId = userProfileFullInfo?.id.orEmpty(),
                            labor = it,
                        ).subscribe({
                            viewState.carrierSuccessUpdated()
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

    fun setcarrierSettlement(settlement: String) {
        currentcarrier.city = settlement
        if (settlement.isNotNullOrEmpty()) {
            currentcarrier.country?.id?.let {
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


    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
    }

    fun setEndYear(year: String) {
        calendar.set(year.toInt(), Calendar.JULY, 31)
        currentcarrier.endWork = calendar.toZonedDateTime().toLocalDate().toString()
    }

    fun setStartYear(year: String) {
        calendar.set(year.toInt(), Calendar.JULY, 31)
        currentcarrier.startWork = calendar.toZonedDateTime().toLocalDate().toString()
    }

    fun setRank(rank: String) {
        currentcarrier.position = rank
    }

    fun setOrg(orgName: String) {
        currentcarrier.orgName = orgName
    }

    fun setWorkDirection(workDirection: String) {

    }

    fun setSelectedCountry(country: Country) {
        currentcarrier.country = country
        currentcarrier.let { viewState.setCurrentCarrierInfo(it) }
    }

    enum class ScreenActionType {
        CREATE,
        EDIT
    }

}
