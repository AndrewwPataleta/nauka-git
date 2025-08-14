package uddug.com.naukoteka.ui.fragments.profile.edit

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import androidx.navigation.fragment.findNavController
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import uddug.com.domain.entities.profile.UserProfileFullInfo
import uddug.com.naukoteka.R
import uddug.com.naukoteka.databinding.FragmentProfileEditAcademicDegreeBinding
import uddug.com.naukoteka.global.base.BaseFragment
import uddug.com.naukoteka.presentation.profile.AcademicDegreeModel
import uddug.com.naukoteka.presentation.profile.edit.ProfileEditAcademicDegreePresenter
import uddug.com.naukoteka.presentation.profile.edit.ProfileEditAcademicDegreeView
import uddug.com.naukoteka.presentation.profile.navigation.ContainerNavigationView
import uddug.com.naukoteka.ui.activities.main.ContainerActivity.Companion.PROFILE_ARGS
import uddug.com.naukoteka.ui.custom.AcademicDegreeEditView
import uddug.com.naukoteka.utils.getCurrentDateTime
import uddug.com.naukoteka.utils.toString
import uddug.com.naukoteka.utils.viewBinding
import java.util.Calendar


class ProfileEditAcademicDegreeFragment :
    BaseFragment(R.layout.fragment_profile_edit_academic_degree),
    ProfileEditAcademicDegreeView {

    override val contentView: FragmentProfileEditAcademicDegreeBinding by viewBinding(
        FragmentProfileEditAcademicDegreeBinding::bind
    )

    @InjectPresenter
    lateinit var presenter: ProfileEditAcademicDegreePresenter

    private var navigationView: ContainerNavigationView? = null

    private var pulseAnimation: Animation? = null

    private var academicDegrees: MutableList<AcademicDegreeEditView> = mutableListOf()

    @ProvidePresenter
    fun providePresenter(): ProfileEditAcademicDegreePresenter {
        return getScope().getInstance(ProfileEditAcademicDegreePresenter::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_profile_edit_academic_degree, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.getParcelable<UserProfileFullInfo>(PROFILE_ARGS)
            ?.let { presenter.setProfileFullInfo(it) }
        contentView.done.setOnClickListener {
            presenter.askToSaveNewAcademicDegree()
        }
        contentView.back.setOnClickListener {
            findNavController().popBackStack()
        }
        contentView.addDegree.setOnClickListener {
            presenter.askToAddNewAcademicDegree()
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
        contentView.degreeContainer.removeAllViews()
        profileInfo.userAcademicDegree.map { academic ->
            val view = AcademicDegreeEditView(
                context = requireContext(),
            ).apply {
                attachAcademicDegree(
                    id = academic.id,
                    academicDegree = academic.name ?: "",
                    date = academic.titleDate ?:  getCurrentDateTime().toString(dateFormat.toString()),
                    parent = contentView.degreeContainer,
                )
            }
            academicDegrees.add(view)
            contentView.degreeContainer.addView(view)
        }
    }

    override fun addNewAcademicDegree() {
        val view = AcademicDegreeEditView(
            context = requireContext(),
        ).apply {
            val currentDate = getCurrentDateTime().toString("yyyy-MM-dd")
            attachAcademicDegree(
                id = null,
                academicDegree = "",
                date = currentDate,
                parent = contentView.degreeContainer,
            )
        }
        academicDegrees.add(view)
        contentView.degreeContainer.addView(view)
    }

    override fun getListDegrees(listDegrees: (List<AcademicDegreeModel>) -> Unit) {
        listDegrees(academicDegrees.map {
            it.getValue()
        })
    }

}
