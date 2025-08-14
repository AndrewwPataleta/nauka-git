package uddug.com.naukoteka.ui.fragments.county

import io.reactivex.disposables.CompositeDisposable
import moxy.InjectViewState
import toothpick.InjectConstructor
import uddug.com.domain.entities.country.Country
import uddug.com.domain.entities.profile.UserProfileFullInfo
import uddug.com.domain.interactors.country.LocationInteractor
import uddug.com.naukoteka.global.base.BasePresenterImpl
import uddug.com.naukoteka.presentation.profile.edit.ProfileEditAddressesListPresenter
import uddug.com.naukoteka.presentation.profile.edit.models.CountryType
import uddug.com.naukoteka.utils.text.isNotNullOrEmpty
import java.util.Locale


@InjectConstructor
@InjectViewState
class CountrySelectPresenter(
    private val locationInteractor: LocationInteractor,
) : BasePresenterImpl<CountrySelectView>() {


    companion object {

    }

    var compositeDisposable: CompositeDisposable = CompositeDisposable()

    var userProfileFullInfo: UserProfileFullInfo? = null

    private var selectedCountryId: String? = null

    private var searchQuery: String = ""

    private val stableCounties: MutableList<Country> = mutableListOf()

    private var searchCounties: List<Country> = listOf()

    private var countrySelectType: CountrySelectType = CountrySelectType.WITH_RESULT

    private var countryType: CountryType? = null

    fun setProfileFullInfo(profileFullInfo: UserProfileFullInfo) {
        this.userProfileFullInfo = profileFullInfo
    }


    fun initSelectedCountryId(countryId: String?, countrySelectType: CountryType?) {
        countryType = countrySelectType
        compositeDisposable.add(
            locationInteractor.getCountries()
                .subscribe({
                    stableCounties.clear()
                    stableCounties.addAll(it)
                    selectedCountryId = countryId
                    searchCounties = stableCounties.map {
                        if (it.id == countryId) {
                            it.copy(
                                isSelected = true
                            )

                        } else {
                            it.copy(
                                isSelected = false
                            )
                        }
                    }
                    viewState.setCountries(searchCounties)
                }, {

                })
        )


    }

    fun setSearchQueryCountry(str: String) {
        searchQuery = str
        searchCounties =
            stableCounties.map {
                if (it.id == selectedCountryId) {
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
                    it.term.orEmpty().lowercase(Locale.ROOT).trim()
                        .contains(searchQuery.lowercase(Locale.getDefault()).trim())
                } else {
                    true
                }
            }
        viewState.setCountries(searchCounties)
    }

    fun updateSelectedCountryId(countryId: String) {
        selectedCountryId = countryId
        searchCounties =
            stableCounties.map {
                if (it.id == selectedCountryId) {
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
                    it.term.orEmpty().lowercase(Locale.ROOT).trim()
                        .contains(searchQuery.lowercase(Locale.getDefault()).trim())
                } else {
                    true
                }
            }
        viewState.setCountries(searchCounties)
    }

    fun askForCountryResult() {
        stableCounties.find { it.id == selectedCountryId }?.let {
            viewState.sendResult(
                it,
                countryType
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

    enum class CountrySelectType(val type: String) {
        WITH_RESULT("WITH_RESULT"),
        WITH_INPUT("WITH_INPUT")
    }


}
