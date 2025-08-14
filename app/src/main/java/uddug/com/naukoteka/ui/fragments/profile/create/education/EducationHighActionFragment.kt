package uddug.com.naukoteka.ui.fragments.profile.create.education

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
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import com.github.stephenvinouze.materialnumberpickercore.MaterialNumberPicker
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import uddug.com.domain.entities.country.Country
import uddug.com.domain.entities.country.Settlement
import uddug.com.domain.entities.profile.Education
import uddug.com.domain.entities.profile.UserProfileFullInfo
import uddug.com.naukoteka.R
import uddug.com.naukoteka.databinding.FragmentEducationHighActionBinding
import uddug.com.naukoteka.global.base.BaseFragment
import uddug.com.naukoteka.presentation.education.EducationHighActionPresenter
import uddug.com.naukoteka.presentation.education.EducationHighActionView
import uddug.com.naukoteka.presentation.profile.edit.adapter.EducationAdapter.Companion.dateFormat
import uddug.com.naukoteka.presentation.profile.navigation.ContainerNavigationView
import uddug.com.naukoteka.ui.activities.main.ContainerActivity.Companion.PROFILE_ARGS
import uddug.com.naukoteka.ui.activities.main.ContainerActivity.Companion.SELECTED_COUNTRY_ID
import uddug.com.naukoteka.ui.activities.main.ContainerActivity.Companion.SELECTED_EDUCATION_ID
import uddug.com.naukoteka.ui.fragments.county.CountrySelectFragment.Companion.SELECTED_COUNTRY
import uddug.com.naukoteka.ui.fragments.county.CountrySelectFragment.Companion.SELECTED_COUNTRY_RESULT
import uddug.com.naukoteka.utils.viewBinding
import java.time.LocalDate
import java.util.Calendar

class EducationHighActionFragment : BaseFragment(R.layout.fragment_education_high_action),
    EducationHighActionView {

    override val contentView: FragmentEducationHighActionBinding by viewBinding(
        FragmentEducationHighActionBinding::bind
    )

    @InjectPresenter
    lateinit var presenter: EducationHighActionPresenter

    private var navigationView: ContainerNavigationView? = null

    companion object {
        private const val MIN_YEAR_PICKER = 1920
        private const val DEFAULT_YEAR_PICKER = 2024
    }

    @ProvidePresenter
    fun providePresenter(): EducationHighActionPresenter =
        getScope().getInstance(EducationHighActionPresenter::class.java)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_education_high_action, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupArguments()
        setupListeners()
        setFragmentResultListener(SELECTED_COUNTRY_RESULT) { _, bundle ->
            bundle.getParcelable<Country>(SELECTED_COUNTRY)?.let { presenter.setSelectedCountry(it) }
        }
    }

    private fun setupArguments() {
        arguments?.getParcelable<UserProfileFullInfo>(PROFILE_ARGS)
            ?.let { presenter.setProfileFullInfo(it) }
        arguments?.getString(SELECTED_EDUCATION_ID)
            ?.let { presenter.setCurrentEducationId(it) }
    }

    private fun setupListeners() {
        with(contentView) {
            country.setOnClickListener { presenter.askForOpenCountrySelect() }
            done.setOnClickListener { presenter.selectUpdateEducation() }
            settlement.addTextChangedListener { presenter.setEducationSettlement(it.toString()) }
            school.addTextChangedListener { presenter.setSchool(it.toString()) }
            back.setOnClickListener { findNavController().popBackStack() }
            speciality.addTextChangedListener { presenter.setSpeciality(it.toString()) }
            qualification.adapter = ArrayAdapter(
                requireActivity(),
                android.R.layout.simple_spinner_item,
                listOf(
                    getString(R.string.Bachelor),
                    getString(R.string.Specialty_education),
                    getString(R.string.Master_degree)
                )
            )
            startEducation.setOnClickListener {
                showYearPicker(
                    currentYear = startEducation.text.toString().format(dateFormat).toIntOrNull() ?: DEFAULT_YEAR_PICKER,
                    onYearSelected = { year ->
                        startEducation.text = year.toString()
                        presenter.setStartYear(year.toString())
                    }
                )
            }
            endEducation.setOnClickListener {
                showYearPicker(
                    currentYear = endEducation.text.toString().format(dateFormat).toIntOrNull() ?: DEFAULT_YEAR_PICKER,
                    onYearSelected = { year ->
                        endEducation.text = year.toString()
                        presenter.setEndYear(year.toString())
                    }
                )
            }
        }
    }

    private fun showYearPicker(currentYear: Int, onYearSelected: (Int) -> Unit) {
        val numberPicker = MaterialNumberPicker(
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
            .setView(numberPicker)
            .setNegativeButton(getString(R.string.cancel), null)
            .setPositiveButton(getString(R.string.choose)) { _, _ -> onYearSelected(numberPicker.value) }
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

    override fun setCurrentEducationInfo(education: Education) {
        with(contentView) {
            toolbar.text = getString(R.string.edit_high_education)
            country.text = education.country?.term.toString()
            settlement.setText(education.cityAsString)
            school.setText(education.name)
            speciality.setText(education.specialty)
            education.startDate?.let { startEducation.text = LocalDate.parse(it, dateFormat).year.toString() }
            education.endDate?.let { endEducation.text = LocalDate.parse(it, dateFormat).year.toString() }
        }
    }

    override fun educationSuccessUpdated() {
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
