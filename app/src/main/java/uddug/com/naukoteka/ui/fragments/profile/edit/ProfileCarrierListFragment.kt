package uddug.com.naukoteka.ui.fragments.profile.edit

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import uddug.com.domain.entities.profile.LaborActivities
import uddug.com.domain.entities.profile.UserProfileFullInfo
import uddug.com.naukoteka.R
import uddug.com.naukoteka.databinding.FragmentProfileCarrierListBinding
import uddug.com.naukoteka.global.base.BaseFragment
import uddug.com.naukoteka.presentation.profile.edit.EducationScreenType
import uddug.com.naukoteka.presentation.profile.edit.ProfileCarrierListPresenter
import uddug.com.naukoteka.presentation.profile.edit.ProfileCarrierListView
import uddug.com.naukoteka.presentation.profile.edit.adapter.CarrierAdapter
import uddug.com.naukoteka.presentation.profile.navigation.ContainerNavigationView
import uddug.com.naukoteka.ui.activities.main.ContainerActivity.Companion.PROFILE_ARGS
import uddug.com.naukoteka.ui.activities.main.ContainerActivity.Companion.SELECTED_CARRIER_ID
import uddug.com.naukoteka.ui.activities.main.ContainerActivity.Companion.SELECTED_EDUCATION_ID
import uddug.com.naukoteka.ui.fragments.profile.create.carrier.CarrierActionFragment.Companion.CREATE_CARRIER_RESULT
import uddug.com.naukoteka.ui.fragments.profile.create.carrier.CarrierActionFragment.Companion.CREATE_CARRIER_RESULT_KEY
import uddug.com.naukoteka.ui.fragments.profile.create.education.EducationMiddleActionFragment.Companion.CREATE_EDUCATION_RESULT
import uddug.com.naukoteka.utils.viewBinding


class ProfileCarrierListFragment :
    BaseFragment(R.layout.fragment_profile_carrier_list),
    ProfileCarrierListView {

    override val contentView: FragmentProfileCarrierListBinding by viewBinding(
        FragmentProfileCarrierListBinding::bind
    )

    @InjectPresenter
    lateinit var presenter: ProfileCarrierListPresenter

    private var navigationView: ContainerNavigationView? = null


    @ProvidePresenter
    fun providePresenter(): ProfileCarrierListPresenter {
        return getScope().getInstance(ProfileCarrierListPresenter::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_profile_carrier_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setFragmentResultListener(CREATE_CARRIER_RESULT, { key, bundle ->
            presenter.loadProfile()
        })

        arguments?.getParcelable<UserProfileFullInfo>(PROFILE_ARGS)
            ?.let { presenter.setProfileFullInfo(it) }
        contentView.back.setOnClickListener {
            findNavController().popBackStack()
        }
        contentView.edit.setOnClickListener {
            presenter.askForCreateEducation()
        }
        contentView.addCarrer.setOnClickListener {
            presenter.askForCreateEducation()
        }

    }

    override fun setCarrierItems(educations: List<LaborActivities>) {
        contentView.carrierList.adapter =
            CarrierAdapter(
                onDeleteClick = {
                    presenter.askForDeleteItem(
                        it
                    )
                },
                onDetailClick = {
                    presenter.askForDetailInfoItem(it)
                }
            ).apply { setItems(educations) }
    }

    override fun showDeleteDialog(laborActivities: LaborActivities) {
        val dialog = Dialog(requireActivity())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val window: Window? = dialog.window
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)
        dialog.setContentView(R.layout.dialog_remove_labor)
        (dialog.findViewById(R.id.cancelDeleteBtn) as? View)?.setOnClickListener {
            dialog.dismiss()
        }
        (dialog.findViewById(R.id.deleteConfirmBtn) as? View)?.setOnClickListener {
            dialog.dismiss()
            presenter.confirmDeleteLaborActivities(laborActivities)
        }
        dialog.show()
    }

    override fun showDetailScreen(profileInfo: UserProfileFullInfo, laborId: String?) {
        val bundle = Bundle()
        bundle.putParcelable(PROFILE_ARGS, profileInfo)
        laborId?.let {
            bundle.putString(SELECTED_CARRIER_ID, laborId)
        }
        findNavController().navigate(R.id.profileCarrierAction, bundle)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        navigationView = requireActivity() as ContainerNavigationView
    }

    override fun onResume() {
        super.onResume()
        navigationView?.showNavigationBottomBar(false)
    }

    override fun setMainInformation(profileInfo: UserProfileFullInfo) {

    }

}
