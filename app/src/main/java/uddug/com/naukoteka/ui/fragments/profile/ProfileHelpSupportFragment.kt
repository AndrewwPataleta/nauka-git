package uddug.com.naukoteka.ui.fragments.profile

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.os.bundleOf
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import uddug.com.domain.entities.profile.UserProfileFullInfo
import uddug.com.naukoteka.R
import uddug.com.naukoteka.core.deeplink.launchCustomTabsByUrl
import uddug.com.naukoteka.databinding.FragmentProfileAdditionalActionBinding
import uddug.com.naukoteka.databinding.FragmentProfileHelpSupportBinding
import uddug.com.naukoteka.global.base.BaseBottomSheetDialogFragment
import uddug.com.naukoteka.global.base.BaseFragment
import uddug.com.naukoteka.presentation.profile.HelpSupportView
import uddug.com.naukoteka.presentation.profile.navigation.ContainerView
import uddug.com.naukoteka.presentation.profile.ProfileAdditionalActionPresenter
import uddug.com.naukoteka.presentation.profile.ProfileHelpSupportPresenter
import uddug.com.naukoteka.utils.viewBinding


class ProfileHelpSupportFragment : BaseFragment(R.layout.fragment_profile_help_support),
    HelpSupportView {

    private lateinit var mBehavior: BottomSheetBehavior<FrameLayout>

    private var containerNavigation: ContainerView? = null

    @InjectPresenter
    lateinit var presenter: ProfileHelpSupportPresenter

    @ProvidePresenter
    fun providePresenter(): ProfileHelpSupportPresenter {
        return getScope().getInstance(ProfileHelpSupportPresenter::class.java)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.containerNavigation = requireActivity() as ContainerView
    }

    companion object {

        private const val PROFILE_FULL_INFO_ARGS = "PROFILE_FULL_INFO_ARGS"

        fun newInstance(profileFullInfo: UserProfileFullInfo): ProfileHelpSupportFragment {
            return ProfileHelpSupportFragment().apply {
                arguments = bundleOf(PROFILE_FULL_INFO_ARGS to profileFullInfo)
            }
        }
    }

    override val contentView: FragmentProfileHelpSupportBinding by viewBinding(
        FragmentProfileHelpSupportBinding::bind
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        arguments?.getParcelable<UserProfileFullInfo>(PROFILE_FULL_INFO_ARGS)
            ?.let { presenter.setProfileFullInfo(it) }
        return inflater.inflate(R.layout.fragment_profile_help_support, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(contentView) {
            userAgreementTitle.setOnClickListener {
                requireActivity().launchCustomTabsByUrl(
                    showTitle = true,
                    link = getString(R.string.eula_stage)
                )
            }
            privacyPolicy.setOnClickListener {
                requireActivity().launchCustomTabsByUrl(
                    showTitle = true,
                    link = getString(R.string.policy_pdf)
                )
            }
            licencePolicy.setOnClickListener {
                requireActivity().launchCustomTabsByUrl(
                    showTitle = true,
                    link = getString(R.string.licence_pdf)
                )
            }
            serviceRules.setOnClickListener {
                requireActivity().launchCustomTabsByUrl(
                    showTitle = true,
                    link = getString(R.string.service_rules_pdf_link)
                )
            }
        }
    }


}
