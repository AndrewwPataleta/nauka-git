package uddug.com.naukoteka.presentation.profile.edit

import android.annotation.SuppressLint
import io.reactivex.disposables.CompositeDisposable
import moxy.InjectViewState
import toothpick.InjectConstructor
import uddug.com.domain.entities.country.Country
import uddug.com.domain.entities.profile.UserProfileFullInfo
import uddug.com.domain.interactors.user_profile.UserProfileInteractor
import uddug.com.naukoteka.global.base.BasePresenterImpl
import uddug.com.naukoteka.presentation.profile.edit.models.CountryType
import uddug.com.naukoteka.presentation.profile.edit.models.SettlementType

@InjectConstructor
@InjectViewState
class ProfileEditAddressesListPresenter(
    private val userProfileInteractor: UserProfileInteractor
) : BasePresenterImpl<ProfileEditAddressesListView>() {

    companion object {
        private const val bornCountryType = "20:6"
        private const val liveCountryType = "20:2"
    }

    private val compositeDisposable = CompositeDisposable()

    var userProfileFullInfo: UserProfileFullInfo? = null

    fun setProfileFullInfo(profileFullInfo: UserProfileFullInfo) {
        this.userProfileFullInfo = profileFullInfo
        updateCountryLive()
        updateCountryBorn()
        updateCityLive()
        updateCityBorn()
    }

    fun askForOpenEditCountryBord() {
        addressFor(bornCountryType)?.country?.let { viewState.openSelectCountryForBorn(it) }
    }

    fun askForOpenEditSettlementBord() {
        addressFor(bornCountryType)?.let { addr ->
            addr.country?.let { viewState.openSettlementForBorn(it, addr.cityAsString) }
        }
    }

    fun askForOpenEditCountryLive() {
        addressFor(liveCountryType)?.country?.let { viewState.openSelectCountryForLive(it) }
    }

    fun askForOpenEditSettlementLive() {
        addressFor(liveCountryType)?.let { addr ->
            addr.country?.let { viewState.openSettlementForLive(it, addr.cityAsString) }
        }
    }

    fun selectUpdateUserAddresses() {
        listOf(bornCountryType, liveCountryType).forEach { type ->
            addressFor(type)?.let { addr ->
                compositeDisposable.add(
                    userProfileInteractor.updateAddress(addr).subscribe({
                        viewState.showSuccessToast()
                    }, { it.printStackTrace() })
                )
            }
        }
    }

    fun setSelectedCountry(country: Country, countryType: CountryType) {
        when (countryType) {
            CountryType.BORN -> {
                addressFor(bornCountryType)?.country = country
                updateCountryBorn()
            }
            CountryType.LIVE -> {
                addressFor(liveCountryType)?.country = country
                updateCountryLive()
            }
        }
    }

    fun setSelectedCity(city: String, cityType: SettlementType) {
        when (cityType) {
            SettlementType.BORN -> {
                addressFor(bornCountryType)?.country?.city = city
                updateCityBorn()
            }
            SettlementType.LIVE -> {
                addressFor(liveCountryType)?.country?.city = city
                updateCityLive()
            }
        }
    }

    private fun addressFor(type: String) =
        userProfileFullInfo?.addresses?.firstOrNull { it.cType == type }

    private fun updateCountryBorn() {
        addressFor(bornCountryType)?.country?.let { viewState.setCountryBord(it) }
    }

    private fun updateCityBorn() {
        addressFor(bornCountryType)?.cityAsString?.let { viewState.setSettlementBord(it) }
    }

    private fun updateCityLive() {
        addressFor(liveCountryType)?.cityAsString?.let { viewState.setSettlementLive(it) }
    }

    private fun updateCountryLive() {
        addressFor(liveCountryType)?.country?.let { viewState.setCountryLive(it) }
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }
}
