package uddug.com.naukoteka.ui.fragments.settlement

import io.reactivex.disposables.CompositeDisposable
import moxy.InjectViewState
import toothpick.InjectConstructor
import uddug.com.domain.entities.country.Settlement
import uddug.com.domain.interactors.country.LocationInteractor

import uddug.com.domain.entities.profile.UserProfileFullInfo

import uddug.com.naukoteka.global.base.BasePresenterImpl
import uddug.com.naukoteka.presentation.profile.edit.models.SettlementType
import uddug.com.naukoteka.utils.text.isNotNullOrEmpty
import java.util.Locale


@InjectConstructor
@InjectViewState
class SettlementSelectPresenter(
    private val locationInteractor: LocationInteractor,
) : BasePresenterImpl<SettlementSelectView>() {

    var compositeDisposable: CompositeDisposable = CompositeDisposable()

    var userProfileFullInfo: UserProfileFullInfo? = null

    private var selectedId: String? = null

    private var searchQuery: String = ""

    private val stableSettlements: MutableList<Settlement> = mutableListOf()

    private var searchSettlements: MutableList<Settlement> = mutableListOf()

    private var countryId: String? = null
    private var settlement: String? = null

    fun setProfileFullInfo(profileFullInfo: UserProfileFullInfo) {
        this.userProfileFullInfo = profileFullInfo
    }

    fun initSelectedId(countryId: String, currentSettlement: String?) {
        this.countryId = countryId
        this.settlement = currentSettlement
        compositeDisposable.add(
            locationInteractor.findSettlementsByCountry(
                countryId,
                currentSettlement.orEmpty()
            )
                .subscribe({
                    stableSettlements.clear()
                    stableSettlements.addAll(it)
                    searchSettlements = stableSettlements.map {
                        if (it.id == countryId) {
                            it.copy(
                                isSelected = true
                            )

                        } else {
                            it.copy(
                                isSelected = false
                            )
                        }
                    }.toMutableList()
                    viewState.setSettlements(searchSettlements)
                }, {

                })
        )
    }

    fun setSearchQuery(str: String) {
        countryId?.let {
            locationInteractor.findSettlementsByCountry(it, str)
                .subscribe({
                    searchSettlements.clear()
                    searchSettlements.add(0, Settlement(id = NO_RESULT_ID, socrname = NO_SELECT))
                    searchSettlements.addAll(it)
                    viewState.setSettlements(searchSettlements)
                }, {
                    it.printStackTrace()
                    searchSettlements.clear()
                    searchSettlements.add(0, Settlement(id = NO_RESULT_ID, city = NO_SELECT))
                    viewState.setSettlements(searchSettlements)
                })
        }?.let {
            compositeDisposable.add(
                it
            )
        }
    }

    fun updateSelectedId(settlement: Settlement) {
        this.settlement = settlement.city
        searchSettlements =
            searchSettlements.map {
                if (it.uref == settlement.uref) {
                    it.copy(
                        isSelected = true
                    )

                } else {
                    it.copy(
                        isSelected = false
                    )
                }
            }.filter {
                if (searchQuery.isNotNullOrEmpty()) {
                    it.socrname.orEmpty().lowercase(Locale.ROOT).trim()
                        .contains(searchQuery.lowercase(Locale.getDefault()).trim())
                } else {
                    true
                }
            }.toMutableList()
        viewState.setSettlements(searchSettlements)

    }

    fun askForResult() {
        stableSettlements.find { it.id == selectedId }?.let {
            viewState.sendResult(
                settlement,
                SettlementType.BORN
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
    }

    enum class SelectType(val type: String) {
        WITH_RESULT("WITH_RESULT"),
        WITH_INPUT("WITH_INPUT")
    }

    companion object {
        const val NO_RESULT_ID = "-1"
        const val NO_SELECT = "Без выбора"
    }

}
