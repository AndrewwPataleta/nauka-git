package uddug.com.naukoteka.ui.fragments.profile.edit

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayoutMediator
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import uddug.com.domain.entities.profile.UserProfileFullInfo
import uddug.com.naukoteka.R
import uddug.com.naukoteka.databinding.FragmentProfileEditEducationContainerBinding
import uddug.com.naukoteka.global.base.BaseFragment
import uddug.com.naukoteka.presentation.profile.edit.EducationScreenType
import uddug.com.naukoteka.presentation.profile.edit.ProfileEditEducationContainerPresenter
import uddug.com.naukoteka.presentation.profile.edit.ProfileEditEducationContainerView
import uddug.com.naukoteka.presentation.profile.edit.adapter.EducationTypeAdapter
import uddug.com.naukoteka.presentation.profile.navigation.ContainerNavigationView
import uddug.com.naukoteka.ui.activities.main.ContainerActivity.Companion.PROFILE_ARGS
import uddug.com.naukoteka.ui.custom.AcademicDegreeEditView
import uddug.com.naukoteka.ui.fragments.profile.create.education.EducationMiddleActionFragment.Companion.CREATE_EDUCATION_RESULT
import uddug.com.naukoteka.utils.viewBinding


class ProfileEditEducationContainerFragment :
    BaseFragment(R.layout.fragment_profile_edit_education_container),
    ProfileEditEducationContainerView {

    override val contentView: FragmentProfileEditEducationContainerBinding by viewBinding(
        FragmentProfileEditEducationContainerBinding::bind
    )

    @InjectPresenter
    lateinit var presenter: ProfileEditEducationContainerPresenter

    private var navigationView: ContainerNavigationView? = null

    private var pulseAnimation: Animation? = null

    private var academicDegrees: MutableList<AcademicDegreeEditView> = mutableListOf()

    @ProvidePresenter
    fun providePresenter(): ProfileEditEducationContainerPresenter {
        return getScope().getInstance(ProfileEditEducationContainerPresenter::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_profile_edit_education_container, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setFragmentResultListener(CREATE_EDUCATION_RESULT, { key, bundle ->
            presenter.loadProfile()
        })
        arguments?.getParcelable<UserProfileFullInfo>(PROFILE_ARGS)
            ?.let { presenter.setProfileFullInfo(it) }
        contentView.back.setOnClickListener {
            findNavController().popBackStack()
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

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun setMainInformation(profileInfo: UserProfileFullInfo) {
        contentView.pager.adapter = EducationTypeAdapter(
            this,
            profileInfo
        )
        TabLayoutMediator(contentView.tabs, contentView.pager) { tab, position ->
            when (position) {
                0 -> tab.text = getString(R.string.middle)
                1 -> tab.text = getString(R.string.high)
                2 -> tab.text = getString(R.string.additional_short)
            }
        }.attach()
    }

    override fun setNavigationScreen(typeScreen: EducationScreenType) {

    }


}
