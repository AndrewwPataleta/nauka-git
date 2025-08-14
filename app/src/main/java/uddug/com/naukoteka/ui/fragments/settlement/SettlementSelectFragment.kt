package uddug.com.naukoteka.ui.fragments.settlement

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
import uddug.com.domain.entities.country.Settlement
import uddug.com.domain.entities.profile.UserProfileFullInfo
import uddug.com.naukoteka.R
import uddug.com.naukoteka.databinding.FragmentCountrySelectBinding
import uddug.com.naukoteka.databinding.FragmentSettlementSelectBinding
import uddug.com.naukoteka.global.base.BaseFragment
import uddug.com.naukoteka.presentation.profile.edit.models.CountryType
import uddug.com.naukoteka.presentation.profile.edit.models.SettlementType
import uddug.com.naukoteka.presentation.profile.navigation.ContainerNavigationView
import uddug.com.naukoteka.ui.activities.main.ContainerActivity.Companion.PROFILE_ARGS
import uddug.com.naukoteka.ui.activities.main.ContainerActivity.Companion.SELECTED_COUNTRY_ID
import uddug.com.naukoteka.ui.fragments.county.CountrySelectFragment.Companion.CURRENT_SETTLEMENT
import uddug.com.naukoteka.ui.fragments.county.adapter.CountrySelectAdapter
import uddug.com.naukoteka.ui.fragments.settlement.adapter.SettlementSelectAdapter
import uddug.com.naukoteka.utils.textChanges
import uddug.com.naukoteka.utils.viewBinding


class SettlementSelectFragment :
    BaseFragment(R.layout.fragment_settlement_select),
    SettlementSelectView {

    override val contentView: FragmentSettlementSelectBinding by viewBinding(
        FragmentSettlementSelectBinding::bind
    )

    @InjectPresenter
    lateinit var presenter: SettlementSelectPresenter

    private var navigationView: ContainerNavigationView? = null

    private val countrySelectAdapter by lazy {
        SettlementSelectAdapter { item: Settlement ->
            item.uref?.let { presenter.updateSelectedId(item) }
        }
    }

    @ProvidePresenter
    fun providePresenter(): SettlementSelectPresenter {
        return getScope().getInstance(SettlementSelectPresenter::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_settlement_select, container, false)

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.getParcelable<UserProfileFullInfo>(PROFILE_ARGS)
            ?.let { presenter.setProfileFullInfo(it) }
        arguments?.getString(SELECTED_COUNTRY_ID)
            .let {
                presenter.initSelectedId(
                    it.orEmpty(),
                    arguments?.getString(CURRENT_SETTLEMENT)
                )
                arguments?.getString(CURRENT_SETTLEMENT)?.let {
                    contentView.newAddress.setText(it)
                }
            }
        contentView.done.setOnClickListener {
            presenter.askForResult()
        }
        contentView.back.setOnClickListener {
            findNavController().popBackStack()
        }
        contentView.settlementList.adapter = countrySelectAdapter
        contentView.newAddress.textChanges()
            .debounce(300)
            .onEach {
                presenter.setSearchQuery(it.toString())
            }
            .launchIn(lifecycleScope)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        navigationView = requireActivity() as ContainerNavigationView
    }

    override fun onResume() {
        super.onResume()
        navigationView?.showNavigationBottomBar(false)
    }


    companion object {
        const val SELECTED_COUNTRY = "selected_country"
        const val UPDATE_COUNTRY_TYPE = "update_country_type"
        const val SELECTED_COUNTRY_RESULT = "selected_country_result"
        const val COUNTRY_SELECT_TYPE = "COUNTRY_SELECT_TYPE"
        const val COUNTRY_SELECT_TYPE_WITH_SELECT = "COUNTRY_SELECT_TYPE_WITH_SELECT"
        const val COUNTRY_SELECT_TYPE_WITH_INPUT = "COUNTRY_SELECT_TYPE_WITH_INPUT"
    }

    override fun setSettlements(settlement: List<Settlement>) {

        countrySelectAdapter.submitList(settlement.map { it })
    }

    override fun sendResult(city: String?, updateType: SettlementType?) {

    }


}
