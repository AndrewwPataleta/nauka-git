package uddug.com.naukoteka.presentation.profile.edit

import android.annotation.SuppressLint
import android.os.Parcelable
import io.reactivex.Completable
import io.reactivex.CompletableSource
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import kotlinx.android.parcel.Parcelize
import moxy.InjectViewState
import toothpick.InjectConstructor
import uddug.com.domain.entities.country.Country
import uddug.com.domain.entities.profile.UserProfileFullInfo
import uddug.com.domain.interactors.user_profile.UserProfileInteractor
import uddug.com.naukoteka.global.base.BasePresenterImpl
import uddug.com.naukoteka.presentation.profile.edit.models.CountryType
import uddug.com.naukoteka.presentation.profile.edit.models.SettlementType
import uddug.com.naukoteka.utils.text.isNotNullOrEmpty
import java.util.concurrent.TimeUnit


@InjectConstructor
@InjectViewState
class ProfileEditAddressesListPresenter(
    private val userProfileInteractor: UserProfileInteractor
) : BasePresenterImpl<ProfileEditAddressesListView>() {

    companion object {
        private const val errorTag = "ProfileEditPresenterError"
        private const val bornCountryType = "20:6"
        private const val liveCountyType = "20:2"
    }

    var compositeDisposable: CompositeDisposable = CompositeDisposable()

    var userProfileFullInfo: UserProfileFullInfo? = null

    fun setProfileFullInfo(profileFullInfo: UserProfileFullInfo) {
        this.userProfileFullInfo = profileFullInfo
        updateCountryLive()
        updateCountryBorn()
        updateCityLive()
        updateCityBorn()
    }

    fun askForOpenEditCountryBord() {
        userProfileFullInfo?.addresses?.firstOrNull { it.cType == bornCountryType }?.country?.let {
            viewState.openSelectCountryForBorn(
                country = it
            )
        }
    }

    fun askForOpenEditSettlementBord() {
        userProfileFullInfo?.addresses?.firstOrNull { it.cType == bornCountryType }?.let {
            it.country?.let { it1 ->
                viewState.openSettlementForBorn(
                    country = it1,
                    settlement = it.cityAsString
                )
            }
        }
    }

    fun askForOpenEditCountryLive() {
        userProfileFullInfo?.addresses?.firstOrNull { it.cType == liveCountyType }?.country?.let {
            viewState.openSelectCountryForLive(
                country = it
            )
        }
    }

    fun askForOpenEditSettlementLive() {
        userProfileFullInfo?.addresses?.firstOrNull { it.cType == liveCountyType }?.let {
            it.country?.let { it1 ->
                viewState.openSettlementForLive(
                    country = it1,
                    settlement = it.cityAsString
                )
            }
        }
    }

    fun selectUpdateUserAddresses() {
        userProfileFullInfo?.addresses?.firstOrNull { it.cType == bornCountryType }?.let {
            compositeDisposable.add(
                userProfileInteractor.updateAddress(
                    it
                ).subscribe({
                    viewState.showSuccessToast()
                }, {
                    it.printStackTrace()
                })
            )
        }
        userProfileFullInfo?.addresses?.firstOrNull { it.cType == liveCountyType }?.let {
            compositeDisposable.add(
                userProfileInteractor.updateAddress(
                    it
                ).subscribe({
                    viewState.showSuccessToast()
                }, {
                    it.printStackTrace()
                })
            )
        }
    }

    fun setSelectedCountry(country: Country, countryType: CountryType) {
        when (countryType) {
            CountryType.BORN -> {
                userProfileFullInfo?.addresses?.firstOrNull { it.cType == bornCountryType }?.country =
                    country
                updateCountryBorn()
            }

            CountryType.LIVE -> {
                userProfileFullInfo?.addresses?.firstOrNull { it.cType == liveCountyType }?.country =
                    country
                updateCountryLive()
            }
        }
    }

    fun setSelectedCity(city: String, cityType: SettlementType) {
        when (cityType) {
            SettlementType.BORN -> {
                userProfileFullInfo?.addresses?.firstOrNull { it.cType == bornCountryType }?.country?.city =
                    city
                updateCityBorn()
            }

            SettlementType.LIVE -> {
                userProfileFullInfo?.addresses?.firstOrNull { it.cType == liveCountyType }?.country?.city =
                    city
                updateCityLive()
            }
        }
    }

    private fun updateCountryBorn() {
        userProfileFullInfo?.addresses?.firstOrNull { it.cType == bornCountryType }?.country?.let {
            viewState.setCountryBord(
                it
            )
        }
    }

    private fun updateCityBorn() {
        userProfileFullInfo?.addresses?.firstOrNull { it.cType == bornCountryType }?.cityAsString?.let {
            viewState.setSettlementBord(
                it
            )
        }
    }

    private fun updateCityLive() {
        userProfileFullInfo?.addresses?.firstOrNull { it.cType == liveCountyType }?.cityAsString?.let {
            viewState.setSettlementLive(
                it
            )
        }
    }

    private fun updateCountryLive() {
        userProfileFullInfo?.addresses?.firstOrNull { it.cType == liveCountyType }?.country?.let {
            viewState.setCountryLive(
                it
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


}
