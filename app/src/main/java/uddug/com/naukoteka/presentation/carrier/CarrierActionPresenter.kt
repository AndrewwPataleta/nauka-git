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
    private var currentCarrierId: String? = null
    private val compositeDisposable = CompositeDisposable()
    private var currentCarrier: LaborActivities = LaborActivities()
    private val calendar = GregorianCalendar()
    private var lastSettlements: List<Settlement> = emptyList()

    var userProfileFullInfo: UserProfileFullInfo? = null

    fun setCurrentcarrierId(carrierId: String) {
        currentCarrierId = carrierId
        currentCarrier.id = carrierId
        screenActionType = ScreenActionType.EDIT
        loadCarrierInfo()
    }

    private fun loadCarrierInfo() {
        userProfileFullInfo?.laborActivity?.find { it.id == currentCarrierId }?.let { carrier ->
            currentCarrier = carrier
            viewState.setCurrentCarrierInfo(carrier)
        }
    }

    fun setProfileFullInfo(profileFullInfo: UserProfileFullInfo) {
        this.userProfileFullInfo = profileFullInfo
    }

    fun askForOpenCountrySelect() {
        viewState.openCountrySelectPage(currentCarrier.country?.id)
    }

    fun selectUpdatecarrier() {
        when (screenActionType) {
            ScreenActionType.CREATE -> {
                if (currentCarrier.country?.id.isNullOrEmpty()) {
                    viewState.showCreateValidationError()
                } else {
                    compositeDisposable.add(
                        userProfileInteractor.createUserLabor(
                            userId = userProfileFullInfo?.id.orEmpty(),
                            labor = currentCarrier,
                        ).subscribe({ viewState.carrierSuccessUpdated() }, {})
                    )
                }
            }
            ScreenActionType.EDIT -> {
                if (currentCarrier.country?.id.isNullOrEmpty()) {
                    viewState.showUpdateValidationError()
                } else {
                    compositeDisposable.add(
                        userProfileInteractor.updateUserCarrier(
                            userId = userProfileFullInfo?.id.orEmpty(),
                            labor = currentCarrier,
                        ).subscribe({ viewState.carrierSuccessUpdated() }, {})
                    )
                }
            }
        }
    }

    fun setcarrierSettlement(settlement: String) {
        currentCarrier.city = settlement
        if (settlement.isNotNullOrEmpty()) {
            currentCarrier.country?.id?.let { countryId ->
                compositeDisposable.add(
                    locationInteractor.findSettlementsByCountry(
                        countryId = countryId,
                        query = settlement,
                    ).subscribe({ settlements ->
                        lastSettlements = settlements
                        viewState.setSettlements(settlements)
                    }, {})
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }

    fun setEndYear(year: String) {
        calendar.set(year.toInt(), Calendar.JULY, 31)
        currentCarrier.endWork = calendar.toZonedDateTime().toLocalDate().toString()
    }

    fun setStartYear(year: String) {
        calendar.set(year.toInt(), Calendar.JULY, 31)
        currentCarrier.startWork = calendar.toZonedDateTime().toLocalDate().toString()
    }

    fun setRank(rank: String) {
        currentCarrier.position = rank
    }

    fun setOrg(orgName: String) {
        currentCarrier.orgName = orgName
    }

    fun setWorkDirection(workDirection: String) {}

    fun setSelectedCountry(country: Country) {
        currentCarrier.country = country
        viewState.setCurrentCarrierInfo(currentCarrier)
    }

    enum class ScreenActionType {
        CREATE,
        EDIT
    }
}
