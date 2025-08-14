package uddug.com.naukoteka.ui.fragments.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import uddug.com.domain.entities.profile.UserProfileFullInfo
import uddug.com.naukoteka.R
import uddug.com.naukoteka.databinding.FragmentProfileFullInfoBinding
import uddug.com.naukoteka.global.base.BaseBottomSheetDialogFragment
import uddug.com.naukoteka.presentation.profile.ProfileFullInfoPresenter
import uddug.com.naukoteka.presentation.profile.ProfileFullView
import uddug.com.naukoteka.ui.custom.CareerExperienceView
import uddug.com.naukoteka.ui.custom.EducationExperienceView
import uddug.com.naukoteka.utils.copyToClipboard
import uddug.com.naukoteka.utils.doIfIsNotNullOrEmpty
import uddug.com.naukoteka.utils.doIfIsNotNullOrEmptyString
import uddug.com.naukoteka.utils.viewBinding
import java.time.LocalDate
import java.time.Month
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle


class ProfileFullInfoBottomSheetFragment : BaseBottomSheetDialogFragment(), ProfileFullView {

    override val contentView by viewBinding(FragmentProfileFullInfoBinding::bind)

    @InjectPresenter
    lateinit var presenter: ProfileFullInfoPresenter

    @ProvidePresenter
    fun providePresenter(): ProfileFullInfoPresenter {
        return getScope().getInstance(ProfileFullInfoPresenter::class.java)
    }

    private var pulseAnimation: Animation? = null

    private val dateFormatterWork = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    companion object {

        private const val PROFILE_FULL_INFO_ARGS = "PROFILE_FULL_INFO_ARGS"

        fun newInstance(profileFullInfo: UserProfileFullInfo): ProfileFullInfoBottomSheetFragment {
            return ProfileFullInfoBottomSheetFragment().apply {
                arguments = bundleOf(PROFILE_FULL_INFO_ARGS to profileFullInfo)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        arguments?.getParcelable<UserProfileFullInfo>(PROFILE_FULL_INFO_ARGS)
            ?.let { presenter.setProfileFullInfo(it) }
        return inflater.inflate(R.layout.fragment_profile_full_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        pulseAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.pulse);

    }

    override fun setProfileFullInfo(profileFullInfo: UserProfileFullInfo) {
        contentView.mainDescription.text = profileFullInfo.dsc
        contentView.subs.text = profileFullInfo.meta?.subscnCount.toString()
        contentView.subr.text = profileFullInfo.meta?.subscrCount.toString()
        profileFullInfo.nickname?.let {
            contentView.link.text = getString(R.string.naukotheka_ru_link, profileFullInfo.nickname)
        }
        contentView.profileLink.setOnClickListener {
            requireActivity().copyToClipboard(getString(R.string.naukotheka_ru_link, it))
            Toast.makeText(
                requireActivity(),
                getString(R.string.link_to_profile_has_been_copied),
                Toast.LENGTH_LONG
            ).show()
        }

//        doIfIsNotNullOrEmpty(profileFullInfo.keywordsMap) {
//            contentView.tagsContainer.isVisible = true
//            it.map { tag ->
//                val tagView = TagInterestView(
//                    context = requireContext(),
//                ).apply {
//                    attachTag(
//                        name = tag.additionalProp?.term.toString(),
//                        parent = contentView.tagsContainer,
//                    )
//                }
//                contentView.tagsContainer.addView(
//                    view
//                )
//                contentView.tagsContainer.addView(tagView)
//            }
//        }
        doIfIsNotNullOrEmptyString(profileFullInfo.birthDate) {
            contentView.birthdayContainer.isVisible
            val dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)
            val dateOfBirth = LocalDate.parse(it)
            val formattedDob = dateOfBirth.format(dateFormatter)
            contentView.birthday.text = formattedDob
        }
        doIfIsNotNullOrEmptyString(profileFullInfo.gender) {
            contentView.genderContainer.isVisible
            contentView.gender.text = it
        }
        doIfIsNotNullOrEmptyString(profileFullInfo.placeOfResidence) {
            contentView.placeBornContainer.isVisible
            contentView.placeBorn.text = it
        }
        doIfIsNotNullOrEmptyString(profileFullInfo.placeOfResidence) {
            contentView.placeLiveContainer.isVisible
            contentView.placeLive.text = it
        }
        doIfIsNotNullOrEmptyString(profileFullInfo.placeOfResidence) {
            contentView.placeLiveContainer.isVisible
            contentView.placeLive.text = it
        }
        doIfIsNotNullOrEmptyString(profileFullInfo.email) {
            contentView.emailContainer.isVisible
            contentView.email.text = it
        }
        contentView.emailContainer.setOnClickListener {
            profileFullInfo.email?.let { it1 -> requireActivity().copyToClipboard(it1) }
            Toast.makeText(
                requireActivity(),
                getString(R.string.email_hash_been_copied),
                Toast.LENGTH_LONG
            ).show()
        }
        doIfIsNotNullOrEmptyString(profileFullInfo.phone) {
            contentView.phoneContainer.isVisible
            contentView.phone.text = it
        }
        doIfIsNotNullOrEmptyString(profileFullInfo.contactDatum.firstOrNull()?.contact) {
            contentView.socialMediaContainer.isVisible
            contentView.socialMedia.text = it
        }

        doIfIsNotNullOrEmpty(profileFullInfo.laborActivity) {
            contentView.careerContainer.isVisible = true
            it.forEach { job ->
                val view = CareerExperienceView(
                    context = requireContext(),
                ).apply {
                    val start = try {
                        (LocalDate.parse(
                            job.startWork.orEmpty(),
                            dateFormatterWork
                        ).year).toString()
                    } catch (e: Exception) {
                        ""
                    }
                    val end = try {
                        (LocalDate.parse(
                            job.startWork.orEmpty(),
                            dateFormatterWork
                        ).year).toString()
                    } catch (e: Exception) {
                        ""
                    }
                    attachWorkExperience(
                        jobId = job.id.toString(),
                        jobName = job.position.toString(),
                        jobPlace = job.orgName.toString(),
                        parent = contentView.careerContainer,
                        jobDate = context.getString(R.string.date_start_params, start, end)
                    )
                }
                contentView.careerContainer.addView(
                    view
                )
            }
        }
        doIfIsNotNullOrEmpty(profileFullInfo.education) {
            contentView.educationContainer.isVisible = true

            it.forEach { education ->
                val studyView = EducationExperienceView(
                    context = requireContext(),
                ).apply {
                    attachStudyExperience(
                        educationName = education.name.orEmpty(),
                        placeWithDate = education.cityAsString.orEmpty(),
                        speciality = education.department.orEmpty(),
                        parent = contentView.educationContainer
                    )

                }
                contentView.educationContainer.addView(
                    studyView
                )
            }
        }
    }

}
