package uddug.com.naukoteka.ui.fragments.profile.edit

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import uddug.com.domain.entities.country.Country
import uddug.com.domain.entities.country.Settlement
import uddug.com.domain.entities.profile.UserProfileFullInfo
import uddug.com.naukoteka.R
import uddug.com.naukoteka.databinding.FragmentProfileEditAddressesListBinding
import uddug.com.naukoteka.global.base.BaseFragment
import uddug.com.naukoteka.utils.viewBinding
import uddug.com.naukoteka.databinding.FragmentProfileEditPersonalIdsBinding
import uddug.com.naukoteka.presentation.profile.edit.ProfileEditAddressesListPresenter
import uddug.com.naukoteka.presentation.profile.edit.ProfileEditAddressesListView
import uddug.com.naukoteka.presentation.profile.edit.ProfileEditPersonalIdsPresenter
import uddug.com.naukoteka.presentation.profile.edit.ProfileEditPersonalIdsView
import uddug.com.naukoteka.presentation.profile.edit.models.CountryType
import uddug.com.naukoteka.presentation.profile.edit.models.SettlementType
import uddug.com.naukoteka.presentation.profile.navigation.ContainerNavigationView
import uddug.com.naukoteka.ui.activities.main.ContainerActivity
import uddug.com.naukoteka.ui.activities.main.ContainerActivity.Companion.SELECTED_COUNTRY_ID
import uddug.com.naukoteka.ui.fragments.county.CountrySelectFragment.Companion.CURRENT_SETTLEMENT
import uddug.com.naukoteka.ui.fragments.county.CountrySelectFragment.Companion.SELECTED_CITY_RESULT
import uddug.com.naukoteka.ui.fragments.county.CountrySelectFragment.Companion.SELECTED_COUNTRY
import uddug.com.naukoteka.ui.fragments.county.CountrySelectFragment.Companion.SELECTED_COUNTRY_RESULT
import uddug.com.naukoteka.ui.fragments.county.CountrySelectFragment.Companion.UPDATE_COUNTRY_TYPE
import uddug.com.naukoteka.ui.fragments.county.CountrySelectFragment.Companion.UPDATE_SETTLEMENT_TYPE

class ProfileEditAddressesListFragment :
    BaseFragment(R.layout.fragment_profile_edit_addresses_list),
    ProfileEditAddressesListView {

    companion object {
        private const val FRAGMENT_FULL_INFO_TAG = "FRAGMENT_FULL_INFO_TAG"
    }

    override val contentView by viewBinding(FragmentProfileEditAddressesListBinding::bind)

    private var navigationView: ContainerNavigationView? = null

    @InjectPresenter
    lateinit var presenter: ProfileEditAddressesListPresenter


    @ProvidePresenter
    fun providePresenter(): ProfileEditAddressesListPresenter {
        return getScope().getInstance(ProfileEditAddressesListPresenter::class.java)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        navigationView = requireActivity() as ContainerNavigationView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.getParcelable<UserProfileFullInfo>(ContainerActivity.PROFILE_ARGS)
            ?.let { presenter.setProfileFullInfo(it) }

        contentView.back.setOnClickListener {
            findNavController().popBackStack()
        }
        contentView.done.setOnClickListener {
            presenter.selectUpdateUserAddresses()
        }
        contentView.countryBorn.setOnClickListener {
            presenter.askForOpenEditCountryBord()
        }
        contentView.countryLive.setOnClickListener {
            presenter.askForOpenEditCountryLive()
        }
        contentView.settlementBorn.setOnClickListener {
            presenter.askForOpenEditSettlementBord()
        }
        contentView.settlementLive.setOnClickListener {
            presenter.askForOpenEditSettlementLive()
        }
        setFragmentResultListener(SELECTED_COUNTRY_RESULT) { key, bundle ->
            bundle.getParcelable<Country>(SELECTED_COUNTRY)?.let {
                presenter.setSelectedCountry(
                    country = it,
                    countryType = bundle.getParcelable(UPDATE_COUNTRY_TYPE)
                        ?: CountryType.BORN
                )
            }
        }
        setFragmentResultListener(SELECTED_CITY_RESULT) { key, bundle ->
            bundle.getString(SELECTED_CITY_RESULT)?.let {
                presenter.setSelectedCity(
                    city = it,
                    cityType = bundle.getParcelable(UPDATE_SETTLEMENT_TYPE)
                        ?: SettlementType.BORN
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        navigationView?.showNavigationBottomBar(false)
    }

    override fun setMainInformation(profileInfo: UserProfileFullInfo) {
        //  contentView.countryBorn.text = profileInfo.addresses.first().country?.term.orEmpty()
    }

    override fun setCountryBord(country: Country) {
        contentView.countryBorn.text = country.term.orEmpty()
    }

    override fun setSettlementBord(settlement: String) {
        contentView.settlementBorn.text = settlement
    }

    override fun setCountryLive(country: Country) {
        contentView.countryLive.text = country.term.orEmpty()
    }

    override fun setSettlementLive(settlement: String) {
        contentView.settlementLive.text = settlement
    }

    override fun openSelectCountryForBorn(country: Country) {
        val bundle = Bundle()
        country.let { bundle.putString(SELECTED_COUNTRY_ID, it.id) }
        bundle.putParcelable(UPDATE_COUNTRY_TYPE, CountryType.BORN)
        findNavController().navigate(R.id.countrySelect, bundle)
    }

    override fun openSettlementForBorn(country: Country, settlement: String?) {
        val bundle = Bundle()
        country.let { bundle.putString(SELECTED_COUNTRY_ID, it.id) }
        bundle.putParcelable(UPDATE_SETTLEMENT_TYPE, SettlementType.BORN)
        bundle.putString(CURRENT_SETTLEMENT, settlement)
        findNavController().navigate(R.id.settlementSelect, bundle)
    }

    override fun openSelectCountryForLive(country: Country) {
        val bundle = Bundle()
        country?.let { bundle.putString(SELECTED_COUNTRY_ID, it.id) }
        bundle.putParcelable(UPDATE_COUNTRY_TYPE, CountryType.LIVE)
        findNavController().navigate(R.id.countrySelect, bundle)
    }

    override fun openSettlementForLive(country: Country, settlement: String?) {
        val bundle = Bundle()
        country.let { bundle.putString(SELECTED_COUNTRY_ID, it.id) }
        bundle.putParcelable(UPDATE_COUNTRY_TYPE, SettlementType.LIVE)
        bundle.putString(CURRENT_SETTLEMENT, settlement)
        findNavController().navigate(R.id.settlementSelect, bundle)
    }

    override fun showSuccessToast() {
        Toast.makeText(
            requireContext(),
            getString(R.string.data_success_updated),
            Toast.LENGTH_LONG
        ).show()
    }

}
