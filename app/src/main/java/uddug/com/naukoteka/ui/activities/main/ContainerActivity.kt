package uddug.com.naukoteka.ui.activities.main

import android.app.PictureInPictureParams
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Rational
import android.view.animation.Animation
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.findNavController
import com.yalantis.ucrop.UCrop
import dagger.hilt.android.AndroidEntryPoint
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import uddug.com.domain.entities.profile.UserProfileFullInfo
import uddug.com.naukoteka.R
import uddug.com.naukoteka.databinding.ActivityMainBinding
import uddug.com.naukoteka.flashphoner.FlashphonerEnvironment
import uddug.com.naukoteka.global.base.BaseActivity
import uddug.com.naukoteka.mvvm.call.IncomingCallEvent
import uddug.com.naukoteka.mvvm.call.IncomingCallViewModel
import uddug.com.naukoteka.presentation.profile.navigation.ContainerNavigationView
import uddug.com.naukoteka.presentation.profile.navigation.ContainerPresenter
import uddug.com.naukoteka.presentation.profile.navigation.ContainerView
import uddug.com.naukoteka.ui.call.SingleCallFragment
import uddug.com.naukoteka.utils.viewBinding
import javax.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ContainerActivity : BaseActivity(), ContainerView, ContainerNavigationView {

    override val contentView: ActivityMainBinding by viewBinding(ActivityMainBinding::inflate)


    @InjectPresenter
    lateinit var presenter: ContainerPresenter

    @Inject
    lateinit var flashphonerEnvironment: FlashphonerEnvironment

    private val incomingCallViewModel: IncomingCallViewModel by viewModels()

    private var pulseAnimation: Animation? = null

    private val pictureInPictureParams: PictureInPictureParams?
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            PictureInPictureParams.Builder()
                .setAspectRatio(Rational(9, 16))
                .build()
        } else {
            null
        }

    companion object {
        const val PROFILE_ARGS = "profileFullInfo"
        const val FEED_ARGS = "profileFeedArgs"
        const val IMAGE_TYPE_PARAM = "imageType"
        const val IMAGE_TYPE_BANNER = "imageTypeBanner"
        const val IMAGE_TYPE_AVATAR = "imageTypeAvatar"
        const val SELECTED_EDUCATION_ID = "selectedEducationId"
        const val SELECTED_CARRIER_ID = "selectedEducationId"
        const val SELECTED_COUNTRY_ID = "selectedCountryId"
        const val EDUCATION_SCREEN_TYPE = "education_screen_type"
        const val DYNAMIC_SETTINGS_FORM = "dynamic_settings_form"
    }

    @ProvidePresenter
    fun providePresenter(): ContainerPresenter {
        return getScope().getInstance(ContainerPresenter::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(contentView.root)

        flashphonerEnvironment.attachContainerActivity(this)
        flashphonerEnvironment.ensureInitialised(this)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                incomingCallViewModel.events.collect { event ->
                    handleIncomingCall(event)
                }
            }
        }

        contentView.bottomNav.setOnNavigationItemSelectedListener {
            val navController = findNavController(R.id.main_nav_host_fragment)

            when (it.itemId) {
                R.id.sphere -> {
                    if (navController.graph.id != R.navigation.nav_graph_sphere) {
                        navController.setGraph(R.navigation.nav_graph_sphere)
                    }
                    contentView.bottomNav.menu.getItem(1).setChecked(true);
                    true
                }

                R.id.nauProfile -> {
                    if (navController.graph.id != R.id.nav_graph_profile) {
                        navController.setGraph(R.navigation.nav_graph_profile)
                    }
                    contentView.bottomNav.menu.getItem(4).setChecked(true);
                    true
                }
                R.id.nauChat -> {
                    if (navController.graph.id != R.id.nav_graph_chat) {
                        navController.setGraph(R.navigation.nav_graph_chat)
                    }
                    contentView.bottomNav.menu.getItem(3).setChecked(true);
                    true
                }

                else -> true
            }
        }
    }

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean, newConfig: Configuration) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        contentView.bottomNav.isVisible = !isInPictureInPictureMode
    }

    fun enterCallPictureInPictureMode(): Boolean {
        val params = pictureInPictureParams ?: return false
        val entered = enterPictureInPictureMode(params)
        if (entered) {
            contentView.bottomNav.isVisible = false
        }
        return entered
    }

    override fun selectShowEditFragment(profileInfo: UserProfileFullInfo) {
        presenter.selectOpenEditFragment(profileInfo)
    }

    override fun showNavigationBottomBar(show: Boolean) {
        contentView.bottomNav.isVisible = show
    }

    override fun openEditFragment(profileFullInfo: UserProfileFullInfo) {
        val bundle = Bundle()
        bundle.putParcelable(PROFILE_ARGS, profileFullInfo)
        findNavController(R.id.main_nav_host_fragment).navigate(R.id.profileEditFragment, bundle)
    }

    override fun openPhotoView(profileFullInfo: UserProfileFullInfo) {
        val bundle = Bundle()
        bundle.putParcelable(PROFILE_ARGS, profileFullInfo)
        findNavController(R.id.main_nav_host_fragment).navigate(
            R.id.profilePhotoViewFragment,
            bundle
        )
    }

    override fun openBannerView(profileFullInfo: UserProfileFullInfo) {
        val bundle = Bundle()
        bundle.putParcelable(PROFILE_ARGS, profileFullInfo)
        bundle.putString(IMAGE_TYPE_PARAM, IMAGE_TYPE_BANNER)
        findNavController(R.id.main_nav_host_fragment).navigate(
            R.id.profilePhotoViewFragment,
            bundle
        )
    }

    override fun openAppSettings(profileFullInfo: UserProfileFullInfo) {
        val bundle = Bundle()
        bundle.putParcelable(PROFILE_ARGS, profileFullInfo)
        findNavController(R.id.main_nav_host_fragment).navigate(
            R.id.appSettingsFragment,
            bundle
        )
    }

    override fun openSupportWithHelp(profileFullInfo: UserProfileFullInfo) {
        val bundle = Bundle()
        bundle.putParcelable(PROFILE_ARGS, profileFullInfo)
        findNavController(R.id.main_nav_host_fragment).navigate(
            R.id.supportWithHelpFragment,
            bundle
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            UCrop.REQUEST_CROP -> {
                data?.let {
                    UCrop.getOutput(it)

                }
            }

            else -> {
                for (fragment in supportFragmentManager.fragments) {

                    fragment.onActivityResult(requestCode, resultCode, data)
                }
            }
        }
    }

    private fun handleIncomingCall(event: IncomingCallEvent) {
        val navController = findNavController(R.id.main_nav_host_fragment)
        if (navController.graph.id != R.id.nav_graph_chat) {
            navController.setGraph(R.navigation.nav_graph_chat)
            contentView.bottomNav.menu.getItem(3).setChecked(true)
        }
        navController.navigate(
            R.id.singleCallFragment,
            Bundle().apply {
                putString(SingleCallFragment.ARG_CONTACT_NAME, event.contactName)
                putString(SingleCallFragment.ARG_AVATAR_URL, event.avatarUrl.orEmpty())
                putLong(SingleCallFragment.ARG_DIALOG_ID, event.dialogId)
                putString(SingleCallFragment.ARG_CALL_TITLE, event.callTitle)
                putBoolean(SingleCallFragment.ARG_IS_INCOMING_CALL, true)
            }
        )
    }

}
