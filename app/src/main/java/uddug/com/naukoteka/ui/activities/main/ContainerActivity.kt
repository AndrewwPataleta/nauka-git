package uddug.com.naukoteka.ui.activities.main

import android.content.Intent
import android.os.Bundle
import android.view.animation.Animation
import androidx.core.view.isVisible
import androidx.navigation.findNavController
import com.yalantis.ucrop.UCrop
import dagger.hilt.android.AndroidEntryPoint
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import uddug.com.domain.entities.profile.UserProfileFullInfo
import uddug.com.naukoteka.R
import uddug.com.naukoteka.databinding.ActivityMainBinding
import uddug.com.naukoteka.global.base.BaseActivity
import uddug.com.naukoteka.presentation.profile.navigation.ContainerNavigationView
import uddug.com.naukoteka.presentation.profile.navigation.ContainerPresenter
import uddug.com.naukoteka.presentation.profile.navigation.ContainerView
import uddug.com.naukoteka.utils.viewBinding

@AndroidEntryPoint
class ContainerActivity : BaseActivity(), ContainerView, ContainerNavigationView {

    override val contentView: ActivityMainBinding by viewBinding(ActivityMainBinding::inflate)


    @InjectPresenter
    lateinit var presenter: ContainerPresenter

    private var pulseAnimation: Animation? = null

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

        contentView.bottomNav.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.sphere -> {
                    findNavController(R.id.main_nav_host_fragment).setGraph(R.navigation.nav_graph_sphere)
                    contentView.bottomNav.menu.getItem(1).setChecked(true);
                    true
                }

                R.id.nauProfile -> {
                    findNavController(R.id.main_nav_host_fragment).setGraph(R.navigation.nav_graph_profile)
                    contentView.bottomNav.menu.getItem(4).setChecked(true);
                    true
                }
                R.id.nauChat -> {
                    findNavController(R.id.main_nav_host_fragment).setGraph(R.navigation.nav_graph_chat)
                    contentView.bottomNav.menu.getItem(3).setChecked(true);
                    true
                }

                else -> true
            }
        }
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


}
