package uddug.com.naukoteka.ui.fragments.county

import uddug.com.naukoteka.global.views.InformativeView
import uddug.com.naukoteka.global.views.LoadingView
import moxy.MvpView
import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType
import uddug.com.domain.entities.country.Country
import uddug.com.domain.entities.profile.Education
import uddug.com.naukoteka.presentation.profile.edit.ProfileEditAddressesListPresenter
import uddug.com.naukoteka.presentation.profile.edit.models.CountryType

@StateStrategyType(OneExecutionStateStrategy::class)
interface CountrySelectView : MvpView, LoadingView, InformativeView {

    fun setCountries(countries: List<Country>)

    fun sendResult(country: Country, updateType: CountryType?)
}
