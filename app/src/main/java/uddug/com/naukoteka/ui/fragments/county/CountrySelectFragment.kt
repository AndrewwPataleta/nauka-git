package uddug.com.naukoteka.ui.fragments.county

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import uddug.com.domain.entities.country.Country
import uddug.com.domain.entities.profile.UserProfileFullInfo
import uddug.com.naukoteka.R
import uddug.com.naukoteka.databinding.FragmentCountrySelectBinding
import uddug.com.naukoteka.global.base.BaseFragment
import uddug.com.naukoteka.presentation.profile.edit.ProfileEditAddressesListPresenter
import uddug.com.naukoteka.presentation.profile.edit.models.CountryType
import uddug.com.naukoteka.presentation.profile.navigation.ContainerNavigationView
import uddug.com.naukoteka.ui.activities.main.ContainerActivity.Companion.PROFILE_ARGS
import uddug.com.naukoteka.ui.activities.main.ContainerActivity.Companion.SELECTED_COUNTRY_ID
import uddug.com.naukoteka.ui.fragments.county.adapter.CountrySelectAdapter
import uddug.com.naukoteka.utils.textChanges
import uddug.com.naukoteka.utils.viewBinding


class CountrySelectFragment :
    BaseFragment(R.layout.fragment_country_select),
    CountrySelectView {

    override val contentView: FragmentCountrySelectBinding by viewBinding(
        FragmentCountrySelectBinding::bind
    )

    @InjectPresenter
    lateinit var presenter: CountrySelectPresenter

    private var navigationView: ContainerNavigationView? = null

    private val countrySelectAdapter by lazy {
        CountrySelectAdapter { item: Country ->
            item.id?.let { presenter.updateSelectedCountryId(it) }
        }
    }

    @ProvidePresenter
    fun providePresenter(): CountrySelectPresenter {
        return getScope().getInstance(CountrySelectPresenter::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_country_select, container, false)

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.getParcelable<UserProfileFullInfo>(PROFILE_ARGS)
            ?.let { presenter.setProfileFullInfo(it) }
        arguments?.getString(SELECTED_COUNTRY_ID)
            .let {
                presenter.initSelectedCountryId(
                    it,
                    arguments?.getParcelable(UPDATE_COUNTRY_TYPE)
                )
            }
        contentView.done.setOnClickListener {
            presenter.askForCountryResult()
        }
        contentView.back.setOnClickListener {
            findNavController().popBackStack()
        }
        contentView.countriesList.adapter = countrySelectAdapter
        contentView.newAddress.textChanges()
            .debounce(300)
            .onEach {
                presenter.setSearchQueryCountry(it.toString())
            }
            .launchIn(lifecycleScope)
        contentView.countriesList.adapter = countrySelectAdapter
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        navigationView = requireActivity() as ContainerNavigationView
    }

    override fun onResume() {
        super.onResume()
        navigationView?.showNavigationBottomBar(false)
    }

    override fun setCountries(countries: List<Country>) {
        countrySelectAdapter.submitList(countries)
    }

    override fun sendResult(
        country: Country,
        countryType: CountryType?
    ) {
        setFragmentResult(
            SELECTED_COUNTRY_RESULT, bundleOf(
                SELECTED_COUNTRY to country,
                UPDATE_COUNTRY_TYPE to countryType
            )
        )
        findNavController().popBackStack()
    }

    companion object {
        const val SELECTED_COUNTRY = "selected_country"
        const val UPDATE_COUNTRY_TYPE = "update_country_type"
        const val CURRENT_SETTLEMENT = "current_settlement"
        const val UPDATE_SETTLEMENT_TYPE = "update_country_type"
        const val SELECTED_COUNTRY_RESULT = "selected_country_result"
        const val SELECTED_CITY_RESULT = "selected_city_result"
    }

}
