package uddug.com.naukoteka.ui.fragments.profile.settings

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import uddug.com.domain.entities.country.Country
import uddug.com.domain.entities.profile.UserProfileFullInfo
import uddug.com.naukoteka.R
import uddug.com.naukoteka.databinding.FragmentCountrySelectBinding
import uddug.com.naukoteka.global.base.BaseFragment
import uddug.com.naukoteka.presentation.profile.edit.ProfileEditAddressesListPresenter
import uddug.com.naukoteka.presentation.profile.edit.models.CountryType
import uddug.com.naukoteka.presentation.profile.navigation.ContainerNavigationView
import uddug.com.naukoteka.ui.activities.main.ContainerActivity.Companion.PROFILE_ARGS
import uddug.com.naukoteka.ui.activities.main.ContainerActivity.Companion.SELECTED_COUNTRY_ID
import uddug.com.naukoteka.ui.fragments.county.adapter.CountrySelectAdapter
import uddug.com.naukoteka.utils.textChanges
import uddug.com.naukoteka.utils.viewBinding


class UserBlockListFragment :
    BaseFragment(R.layout.fragment_block_users_list),
    UserBlockListView {

    override val contentView: FragmentCountrySelectBinding by viewBinding(
        FragmentCountrySelectBinding::bind
    )

    private var blockListAdapter: UserBlockListAdapter? = null

    @InjectPresenter
    lateinit var presenter: UserBlockListPresenter

    private var navigationView: ContainerNavigationView? = null


    @ProvidePresenter
    fun providePresenter(): UserBlockListPresenter {
        return getScope().getInstance(UserBlockListPresenter::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_block_users_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.getParcelable<UserProfileFullInfo>(PROFILE_ARGS)
            ?.let { presenter.setProfileFullInfo(it) }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        navigationView = requireActivity() as ContainerNavigationView
    }

    override fun onResume() {
        super.onResume()
        navigationView?.showNavigationBottomBar(false)
    }

}
