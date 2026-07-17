package uddug.com.naukoteka.ui.fragments.profile.create.carrier

import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.NumberPicker
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import com.github.stephenvinouze.materialnumberpickercore.MaterialNumberPicker
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import uddug.com.domain.entities.country.Country
import uddug.com.domain.entities.country.Settlement
import uddug.com.domain.entities.profile.LaborActivities
import uddug.com.domain.entities.profile.UserProfileFullInfo
import uddug.com.naukoteka.R
import uddug.com.naukoteka.databinding.FragmentCarrierActionBinding
import uddug.com.naukoteka.global.base.BaseFragment
import uddug.com.naukoteka.presentation.carrier.CarrierActionPresenter
import uddug.com.naukoteka.presentation.carrier.CarrierActionView
import uddug.com.naukoteka.presentation.profile.edit.adapter.EducationAdapter.Companion.dateFormat
import uddug.com.naukoteka.presentation.profile.navigation.ContainerNavigationView
import uddug.com.naukoteka.ui.activities.main.ContainerActivity.Companion.PROFILE_ARGS
import uddug.com.naukoteka.ui.activities.main.ContainerActivity.Companion.SELECTED_CARRIER_ID
import uddug.com.naukoteka.ui.activities.main.ContainerActivity.Companion.SELECTED_COUNTRY_ID
import uddug.com.naukoteka.ui.fragments.county.CountrySelectFragment.Companion.SELECTED_COUNTRY
import uddug.com.naukoteka.ui.fragments.county.CountrySelectFragment.Companion.SELECTED_COUNTRY_RESULT
import uddug.com.naukoteka.utils.viewBinding
import java.time.LocalDate
import java.util.Calendar

class CarrierActionFragment :
    BaseFragment(R.layout.fragment_carrier_action),
    CarrierActionView {

    override val contentView: FragmentCarrierActionBinding by viewBinding(
        FragmentCarrierActionBinding::bind
    )

    @InjectPresenter
    lateinit var presenter: CarrierActionPresenter

    private var navigationView: ContainerNavigationView? = null

    companion object {
        private const val MIN_YEAR_PICKER = 1920
        private const val DEFAULT_YEAR_PICKER = 2024
        const val CREATE_CARRIER_RESULT = "create_carrier_result"
        const val CREATE_CARRIER_RESULT_KEY = "create_carrier_result_key"
    }

    @ProvidePresenter
    fun providePresenter(): CarrierActionPresenter =
        getScope().getInstance(CarrierActionPresenter::class.java)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_carrier_action, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.getParcelable<UserProfileFullInfo>(PROFILE_ARGS)
            ?.let { presenter.setProfileFullInfo(it) }
        arguments?.getString(SELECTED_CARRIER_ID)
            ?.let { presenter.setCurrentcarrierId(it) }
        with(contentView) {
            country.setOnClickListener { presenter.askForOpenCountrySelect() }
            done.setOnClickListener { presenter.selectUpdatecarrier() }
            settlement.addTextChangedListener { presenter.setcarrierSettlement(it.toString()) }
            rank.addTextChangedListener { presenter.setRank(it.toString()) }
            placeWork.addTextChangedListener { presenter.setOrg(it.toString()) }
            workDirection.addTextChangedListener { presenter.setWorkDirection(it.toString()) }
            back.setOnClickListener { findNavController().popBackStack() }
            startLabor.setOnClickListener {
                showYearPicker(
                    currentYear = startLabor.text.toString().toIntOrNull() ?: DEFAULT_YEAR_PICKER,
                    onYearSelected = { year ->
                        startLabor.text = year.toString()
                        presenter.setStartYear(year.toString())
                    }
                )
            }
            endLabor.setOnClickListener {
                showYearPicker(
                    currentYear = endLabor.text.toString().toIntOrNull() ?: DEFAULT_YEAR_PICKER,
                    onYearSelected = { year ->
                        endLabor.text = year.toString()
                        presenter.setEndYear(year.toString())
                    }
                )
            }
        }
        setFragmentResultListener(SELECTED_COUNTRY_RESULT) { _, bundle ->
            bundle.getParcelable<Country>(SELECTED_COUNTRY)?.let { presenter.setSelectedCountry(it) }
        }
    }

    private fun showYearPicker(currentYear: Int, onYearSelected: (Int) -> Unit) {
        val picker = MaterialNumberPicker(
            context = requireActivity(),
            minValue = MIN_YEAR_PICKER,
            maxValue = Calendar.getInstance().get(Calendar.YEAR),
            value = currentYear,
            separatorColor = ContextCompat.getColor(requireActivity(), android.R.color.transparent),
            textStyle = Typeface.BOLD_ITALIC,
            editable = false,
            wrapped = false,
            formatter = NumberPicker.Formatter { it.toString() }
        )
        AlertDialog.Builder(requireActivity())
            .setTitle(getString(R.string.select_year))
            .setView(picker)
            .setNegativeButton(getString(R.string.cancel), null)
            .setPositiveButton(getString(R.string.choose)) { _, _ -> onYearSelected(picker.value) }
            .show()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        navigationView = requireActivity() as ContainerNavigationView
    }

    override fun onResume() {
        super.onResume()
        navigationView?.showNavigationBottomBar(false)
    }

    override fun openCountrySelectPage(selectedCountryId: String?) {
        val bundle = Bundle().apply { selectedCountryId?.let { putString(SELECTED_COUNTRY_ID, it) } }
        findNavController().navigate(R.id.countrySelect, bundle)
    }

    override fun setCurrentCarrierInfo(labor: LaborActivities) {
        with(contentView) {
            toolbar.text = getString(R.string.edit_carrier)
            country.text = labor.country?.term.toString()
            settlement.setText(labor.cityAsString)
            placeWork.setText(labor.orgName)
            rank.setText(labor.position)
            workDirection.setText(labor.activityAreasMap.values.firstOrNull())
            labor.startWork?.let { startLabor.text = LocalDate.parse(it, dateFormat).year.toString() }
            labor.endWork?.let { endLabor.text = LocalDate.parse(it, dateFormat).year.toString() }
        }
    }

    override fun carrierSuccessUpdated() {
        setFragmentResult(CREATE_CARRIER_RESULT, bundleOf(CREATE_CARRIER_RESULT_KEY to true))
        findNavController().popBackStack()
    }

    override fun setSettlements(settlements: List<Settlement>) {
        contentView.settlement.setAdapter(
            ArrayAdapter(
                requireActivity(),
                android.R.layout.simple_dropdown_item_1line,
                settlements.map { it.city }
            )
        )
    }

    override fun showUpdateValidationError() {
        Toast.makeText(requireActivity(), getString(R.string.check_correct_fields), Toast.LENGTH_LONG).show()
    }

    override fun showCreateValidationError() {
        Toast.makeText(requireActivity(), getString(R.string.check_correct_fields), Toast.LENGTH_LONG).show()
    }
}
