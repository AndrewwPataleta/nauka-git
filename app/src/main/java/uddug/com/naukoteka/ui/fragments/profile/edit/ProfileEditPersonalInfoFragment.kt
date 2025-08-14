package uddug.com.naukoteka.ui.fragments.profile.edit

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import uddug.com.domain.entities.profile.UserProfileFullInfo
import uddug.com.naukoteka.R
import uddug.com.naukoteka.databinding.FragmentProfileEditPersonalInfoBinding
import uddug.com.naukoteka.global.base.BaseFragment
import uddug.com.naukoteka.presentation.profile.edit.ProfileEditPersonalInfoPresenter
import uddug.com.naukoteka.presentation.profile.edit.ProfileEditPersonalInfoView
import uddug.com.naukoteka.presentation.profile.navigation.ContainerNavigationView
import uddug.com.naukoteka.ui.activities.main.ContainerActivity
import uddug.com.naukoteka.ui.fragments.profile.edit.ProfileAvatarEditActionBottomSheetFragment.Companion.DELETE_AVATAR_RESULT
import uddug.com.naukoteka.utils.viewBinding
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Calendar
import java.util.Locale


class ProfileEditPersonalInfoFragment : BaseFragment(R.layout.fragment_profile_edit_personal_info),
    ProfileEditPersonalInfoView {

    companion object {
        private const val FRAGMENT_FULL_INFO_TAG = "FRAGMENT_FULL_INFO_TAG"
        const val UPDATE_PROFILE_INFO = "UPDATE_PROFILE_INFO"
        private const val UPDATE_PROFILE_INFO_DATA = "UPDATE_PROFILE_INFO_DATA"
        val dateFormat = "yyyy-MM-dd"
    }

    override val contentView by viewBinding(FragmentProfileEditPersonalInfoBinding::bind)

    private var navigationView: ContainerNavigationView? = null

    @InjectPresenter
    lateinit var presenter: ProfileEditPersonalInfoPresenter


    @ProvidePresenter
    fun providePresenter(): ProfileEditPersonalInfoPresenter {
        return getScope().getInstance(ProfileEditPersonalInfoPresenter::class.java)
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
            presenter.updateProfileShortInfo()
        }
        contentView.nameValue.addTextChangedListener {
            presenter.setFirstName(it.toString())
        }
        contentView.secondNameValue.addTextChangedListener {
            presenter.setSecondName(it.toString())
        }
        contentView.thirdNameValue.addTextChangedListener {
            presenter.setMiddleName(it.toString())
        }
        contentView.descriptionProfileValue.addTextChangedListener {
            presenter.setDescription(it.toString())
        }
        contentView.interestsChoose.addTextChangedListener {
            presenter.searchForInterestsByQuery(it.toString())
        }
    }

    override fun onResume() {
        super.onResume()
        navigationView?.showNavigationBottomBar(false)
    }

    override fun setMainInformation(profileInfo: UserProfileFullInfo) {
        contentView.nameValue.setText(profileInfo.firstName ?: "")
        contentView.secondNameValue.setText(profileInfo.lastName ?: "")
        contentView.thirdNameValue.setText(profileInfo.middleName ?: "")
        profileInfo.birthDate?.let {
            val dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)
            val dateOfBirth = LocalDate.parse(it)
            val formattedDob = dateOfBirth.format(dateFormatter)
            contentView.dateBirhdayValue.text = formattedDob

        }
        contentView.done.setOnClickListener {
            presenter.updateProfileShortInfo()
        }
        contentView.descriptionProfileValue.setText(profileInfo.dsc)
        contentView.dateBirhdayValue.setOnClickListener {
            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)
            val dpd = DatePickerDialog(
                requireActivity(),
                { view, year, monthOfYear, dayOfMonth ->
                    val date = SimpleDateFormat(dateFormat).format(c.time)
                    contentView.dateBirhdayValue.text = date
                    presenter.setBirthday(date)
                },
                year,
                month,
                day
            )
            dpd.show()
        }
    }

    override fun setMaxInputRange(maxDefault: Int, maxDescription: Int) {
        contentView.nameValue.filters = arrayOf(InputFilter.LengthFilter(maxDefault))
        contentView.secondNameValue.filters = arrayOf(InputFilter.LengthFilter(maxDefault))
        contentView.thirdNameValue.filters = arrayOf(InputFilter.LengthFilter(maxDefault))
        contentView.descriptionProfileValue.filters =
            arrayOf(InputFilter.LengthFilter(maxDescription))
    }

    override fun updateLengthInputs(maxDefault: Int, maxDescription: Int) {
        contentView.nameFromTo.text = resources.getString(
            R.string.length_from_to,
            contentView.nameValue.length().toString(),
            maxDefault.toString()
        )
        contentView.seconaNameFromTo.text = resources.getString(
            R.string.length_from_to,
            contentView.secondNameValue.length().toString(),
            maxDefault.toString()
        )
        contentView.thirdFromTo.text = resources.getString(
            R.string.length_from_to,
            contentView.thirdNameValue.length().toString(),
            maxDefault.toString()
        )
        contentView.descriptionFromTo.text = resources.getString(
            R.string.length_from_to,
            contentView.descriptionProfileValue.length().toString(),
            maxDescription.toString()
        )
    }

    override fun setGenders(genres: List<String>, selectedPos: Int) {
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item, genres
        )
        contentView.genderChoose.adapter = adapter
        contentView.genderChoose.setSelection(selectedPos)
        contentView.genderChoose.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View, position: Int, id: Long
            ) {
                presenter.setGenderPosition(position)
                contentView.genderChoose.setSelection(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    override fun profileSuccessfulUpdate() {
        setFragmentResult(
            UPDATE_PROFILE_INFO, bundleOf(
                UPDATE_PROFILE_INFO_DATA to true
            )
        )
        findNavController().popBackStack()
    }


}
