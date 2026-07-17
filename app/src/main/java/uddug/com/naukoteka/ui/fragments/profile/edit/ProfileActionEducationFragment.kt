package uddug.com.naukoteka.ui.fragments.profile.edit

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.navigation.fragment.findNavController
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import uddug.com.domain.entities.profile.Education
import uddug.com.domain.entities.profile.UserProfileFullInfo
import uddug.com.naukoteka.R
import uddug.com.naukoteka.databinding.FragmentProfileEditEducationBinding
import uddug.com.naukoteka.global.base.BaseFragment
import uddug.com.naukoteka.presentation.profile.edit.EducationScreenType
import uddug.com.naukoteka.presentation.profile.edit.ProfileEditEducationPresenter
import uddug.com.naukoteka.presentation.profile.edit.ProfileMiddleActionEducationView
import uddug.com.naukoteka.presentation.profile.edit.adapter.EducationAdapter
import uddug.com.naukoteka.presentation.profile.navigation.ContainerNavigationView
import uddug.com.naukoteka.ui.activities.main.ContainerActivity.Companion.EDUCATION_SCREEN_TYPE
import uddug.com.naukoteka.ui.activities.main.ContainerActivity.Companion.PROFILE_ARGS
import uddug.com.naukoteka.ui.activities.main.ContainerActivity.Companion.SELECTED_EDUCATION_ID
import uddug.com.naukoteka.utils.viewBinding

class ProfileActionEducationFragment :
    BaseFragment(R.layout.fragment_profile_edit_education),
    ProfileMiddleActionEducationView {

    override val contentView: FragmentProfileEditEducationBinding by viewBinding(
        FragmentProfileEditEducationBinding::bind
    )

    @InjectPresenter
    lateinit var presenter: ProfileEditEducationPresenter

    private var navigationView: ContainerNavigationView? = null

    @ProvidePresenter
    fun providePresenter(): ProfileEditEducationPresenter =
        getScope().getInstance(ProfileEditEducationPresenter::class.java)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_profile_edit_education, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.getString(EDUCATION_SCREEN_TYPE)?.let { presenter.setEducationType(it) }
        arguments?.getParcelable<UserProfileFullInfo>(PROFILE_ARGS)?.let { presenter.setProfileFullInfo(it) }
        contentView.addEducation.setOnClickListener { presenter.askForAddNewEducation() }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        navigationView = requireActivity() as ContainerNavigationView
    }

    override fun onResume() {
        super.onResume()
        navigationView?.showNavigationBottomBar(false)
    }

    override fun setEducationItems(educations: List<Education>) {
        contentView.educationList.adapter = EducationAdapter(
            onDeleteClick = { presenter.askForDeleteItem(it) },
            onDetailClick = { presenter.askForDetailInfoItem(it) }
        ).apply { setItems(educations) }
    }

    override fun showDeleteDialog(education: Education) {
        Dialog(requireActivity()).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            setCancelable(true)
            setCanceledOnTouchOutside(true)
            setContentView(R.layout.dialog_remove_education)
            (findViewById(R.id.cancelDeleteBtn) as? View)?.setOnClickListener { dismiss() }
            (findViewById(R.id.deleteConfirmBtn) as? View)?.setOnClickListener {
                dismiss()
                presenter.confirmDeleteEducation(education)
            }
        }.show()
    }

    override fun showDetailScreen(profileInfo: UserProfileFullInfo, educationId: String?, type: EducationScreenType) {
        navigateToEducation(profileInfo, educationId, type)
    }

    override fun showAddNewEducation(profileInfo: UserProfileFullInfo, type: EducationScreenType) {
        navigateToEducation(profileInfo, null, type)
    }

    private fun navigateToEducation(profileInfo: UserProfileFullInfo, educationId: String?, type: EducationScreenType) {
        val destination = when (type) {
            EducationScreenType.MIDDLE -> R.id.educationMiddleActionFragment
            EducationScreenType.HIGH -> R.id.educationHighActionFragment
            EducationScreenType.ADDITIONAL -> R.id.educationAdditionalActionFragment
        }
        val bundle = Bundle()
        bundle.putParcelable(PROFILE_ARGS, profileInfo)
        educationId?.let { bundle.putString(SELECTED_EDUCATION_ID, it) }
        findNavController().navigate(destination, bundle)
    }
}
