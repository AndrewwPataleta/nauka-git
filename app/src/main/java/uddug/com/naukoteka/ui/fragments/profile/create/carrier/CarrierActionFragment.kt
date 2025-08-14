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
import uddug.com.domain.entities.profile.Education
import uddug.com.domain.entities.profile.LaborActivities
import uddug.com.domain.entities.profile.UserProfileFullInfo
import uddug.com.naukoteka.R
import uddug.com.naukoteka.databinding.FragmentCarrierActionBinding
import uddug.com.naukoteka.databinding.FragmentEducationMiddleActionBinding
import uddug.com.naukoteka.global.base.BaseFragment
import uddug.com.naukoteka.presentation.carrier.CarrierActionPresenter
import uddug.com.naukoteka.presentation.carrier.CarrierActionView
import uddug.com.naukoteka.presentation.education.EducationMiddleActionPresenter
import uddug.com.naukoteka.presentation.education.EducationMiddleActionView
import uddug.com.naukoteka.presentation.profile.edit.adapter.EducationAdapter.Companion.dateFormat
import uddug.com.naukoteka.presentation.profile.navigation.ContainerNavigationView
import uddug.com.naukoteka.ui.activities.main.ContainerActivity.Companion.PROFILE_ARGS
import uddug.com.naukoteka.ui.activities.main.ContainerActivity.Companion.SELECTED_CARRIER_ID
import uddug.com.naukoteka.ui.activities.main.ContainerActivity.Companion.SELECTED_COUNTRY_ID
import uddug.com.naukoteka.ui.activities.main.ContainerActivity.Companion.SELECTED_EDUCATION_ID
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
    fun providePresenter(): CarrierActionPresenter {
        return getScope().getInstance(CarrierActionPresenter::class.java)
    }

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
        contentView.country.setOnClickListener {
            presenter.askForOpenCountrySelect()
        }
        contentView.done.setOnClickListener {
            presenter.selectUpdatecarrier()
        }
        contentView.settlement.addTextChangedListener {
            presenter.setcarrierSettlement(it.toString())
        }
        contentView.rank.addTextChangedListener {
            presenter.setRank(it.toString())
        }
        contentView.placeWork.addTextChangedListener {
            presenter.setOrg(it.toString())
        }
        contentView.workDirection.addTextChangedListener {
            presenter.setWorkDirection(it.toString())
        }

        contentView.back.setOnClickListener {
            findNavController().popBackStack()
        }

        contentView.startLabor.setOnClickListener {
            val numberPicker = MaterialNumberPicker(
                context = requireActivity(),
                minValue = MIN_YEAR_PICKER,
                maxValue = Calendar.getInstance().get(Calendar.YEAR),
                value = contentView.startLabor.text.toString().format(
                    dateFormat
                ).toIntOrNull() ?: DEFAULT_YEAR_PICKER,
                separatorColor = ContextCompat.getColor(
                    requireActivity(),
                    android.R.color.transparent
                ),
                textStyle = Typeface.BOLD_ITALIC,
                editable = false,
                wrapped = false,
                formatter = NumberPicker.Formatter {
                    return@Formatter "${it}"
                }
            )
            AlertDialog.Builder(requireActivity())
                .setTitle(requireActivity().getString(R.string.select_year))
                .setView(numberPicker)
                .setNegativeButton(requireActivity().getString(R.string.cancel), null)
                .setPositiveButton(requireActivity().getString(R.string.choose)) { _, _ ->
                    contentView.startLabor.text = numberPicker.value.toString()
                    presenter.setStartYear(numberPicker.value.toString())
                }
                .show()
        }
        contentView.endLabor.setOnClickListener {
            val numberPicker = MaterialNumberPicker(
                context = requireActivity(),
                minValue = MIN_YEAR_PICKER,
                maxValue = Calendar.getInstance().get(Calendar.YEAR),
                value = contentView.endLabor.text.toString().format(
                    dateFormat
                ).toIntOrNull() ?: DEFAULT_YEAR_PICKER,
                separatorColor = ContextCompat.getColor(
                    requireActivity(),
                    android.R.color.transparent
                ),
                textStyle = Typeface.BOLD_ITALIC,
                editable = false,
                wrapped = false,
                formatter = NumberPicker.Formatter {
                    return@Formatter "${it}"
                }
            )
            AlertDialog.Builder(requireActivity())
                .setTitle(requireActivity().getString(R.string.select_year))
                .setView(numberPicker)
                .setNegativeButton(requireActivity().getString(R.string.cancel), null)
                .setPositiveButton(requireActivity().getString(R.string.choose)) { _, _ ->
                    contentView.endLabor.text = numberPicker.value.toString()
                    presenter.setEndYear(numberPicker.value.toString())
                }
                .show()
        }
        setFragmentResultListener(SELECTED_COUNTRY_RESULT) { key, bundle ->
            bundle.getParcelable<Country>(SELECTED_COUNTRY)?.let {
                presenter.setSelectedCountry(
                    it
                )
            }
        }
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
        val bundle = Bundle()
        selectedCountryId?.let { bundle.putString(SELECTED_COUNTRY_ID, it) }
        findNavController().navigate(R.id.countrySelect, bundle)
    }

    override fun setCurrentCarrierInfo(labor: LaborActivities) {
        contentView.toolbar.text = getString(R.string.edit_carrier)
        contentView.country.text = labor.country?.term.toString()
        contentView.settlement.setText(labor.cityAsString)
        contentView.placeWork.setText(labor.orgName)
        contentView.rank.setText(labor.position)
        contentView.workDirection.setText(labor.activityAreasMap.values.firstOrNull())

        labor.startWork?.let {
            contentView.startLabor.text =
                LocalDate.parse(labor.startWork, dateFormat).year.toString()
        }
        labor.endWork?.let {
            contentView.endLabor.text =
                LocalDate.parse(labor.endWork, dateFormat).year.toString()
        }
    }

    override fun carrierSuccessUpdated() {
        setFragmentResult(
            CREATE_CARRIER_RESULT, bundleOf(
                CREATE_CARRIER_RESULT_KEY to true
            )
        )
        findNavController().popBackStack()
    }

    override fun setSettlements(settlements: List<Settlement>) {
        contentView.settlement.setAdapter(
            ArrayAdapter<String>(
                requireActivity(),
                android.R.layout.simple_dropdown_item_1line, settlements.map { it.city }
            )
        )
    }

    override fun showUpdateValidationError() {
        Toast.makeText(
            requireActivity(),
            getString(R.string.check_correct_fields), Toast.LENGTH_LONG
        )
            .show()
    }

    override fun showCreateValidationError() {
        Toast.makeText(
            requireActivity(),
            getString(R.string.check_correct_fields),
            Toast.LENGTH_LONG
        )
            .show()
    }


}
