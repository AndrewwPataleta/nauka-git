package uddug.com.naukoteka.ui.fragments.profile.settings

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.animation.Animation
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import uddug.com.domain.entities.profile.UserProfileFullInfo
import uddug.com.naukoteka.BuildConfig
import uddug.com.naukoteka.R
import uddug.com.naukoteka.databinding.FragmentProfileEditBinding
import uddug.com.naukoteka.databinding.FragmentSettingsSystemBinding
import uddug.com.naukoteka.global.base.BaseFragment
import uddug.com.naukoteka.presentation.profile.ProfileAvatarActionPresenter
import uddug.com.naukoteka.presentation.profile.edit.ProfileEditPresenter
import uddug.com.naukoteka.presentation.profile.edit.ProfileEditView
import uddug.com.naukoteka.presentation.profile.navigation.ContainerNavigationView
import uddug.com.naukoteka.ui.activities.main.ContainerActivity.Companion.PROFILE_ARGS
import uddug.com.naukoteka.ui.fragments.profile.edit.ProfileAvatarEditActionBottomSheetFragment.Companion.PROFILE_FULL_INFO_ARGS
import uddug.com.naukoteka.ui.fragments.profile.edit.ProfileAvatarEditActionBottomSheetFragment.Companion.UPLOAD_AVATAR_RESULT
import uddug.com.naukoteka.ui.fragments.profile.edit.ProfileEditPersonalInfoFragment.Companion.UPDATE_PROFILE_INFO
import uddug.com.naukoteka.utils.getHashCodeToString
import uddug.com.naukoteka.utils.text.isNotNullOrEmpty
import uddug.com.naukoteka.utils.ui.load
import uddug.com.naukoteka.utils.viewBinding
import java.io.File


class ProfileEditSettingsSystemFragment : BaseFragment(R.layout.fragment_settings_system),
    ProfileSettingsSystemView {

    override val contentView: FragmentSettingsSystemBinding by viewBinding(
        FragmentSettingsSystemBinding::bind
    )

    @InjectPresenter
    lateinit var presenter: ProfileSettingsSystemPresenter

    private var navigationView: ContainerNavigationView? = null

    private var pulseAnimation: Animation? = null

    @ProvidePresenter
    fun providePresenter(): ProfileSettingsSystemPresenter {
        return getScope().getInstance(ProfileSettingsSystemPresenter::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_settings_system, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.getParcelable<UserProfileFullInfo>(PROFILE_ARGS)
            ?.let { presenter.setProfileFullInfo(it) }
        contentView.back.setOnClickListener {
            findNavController().popBackStack()
        }
        contentView.lightTheme.setOnClickListener {
            presenter.selectLightMode()
        }
        contentView.darkTheme.setOnClickListener {
            presenter.selectDarkMode()
        }
        contentView.clearCache.setOnClickListener {
            presenter.selectClearCache()
        }
        contentView.compressImagesSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            presenter.selectCompressImage(isChecked)
        }
        contentView.compressVideoSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            presenter.selectCompressVideoSwitch(isChecked)
        }
        contentView.autoPlayGif.setOnCheckedChangeListener { buttonView, isChecked ->
            presenter.selectAutoPlayGif(isChecked)
        }
        contentView.autoPlayVideoSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            presenter.selectAutoPlayVideoSwitch(isChecked)
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

    override fun setMainInformation(profileInfo: UserProfileFullInfo) {

    }

    override fun showClearCacheSuccess() {
        Toast.makeText(
            requireActivity(),
            getString(R.string.cache_delete_successfull), Toast.LENGTH_LONG
        ).show()
    }

    override fun setCompressImage(compress: Boolean) {
        contentView.compressImagesSwitch.isChecked = compress
    }

    override fun setCompressVideo(compress: Boolean) {
        contentView.compressVideoSwitch.isChecked = compress
    }

    override fun setAutoPlayGif(autoPlay: Boolean) {
        contentView.autoPlayGif.isChecked = autoPlay
    }

    override fun setAutoplayVideo(autoPlay: Boolean) {
        contentView.autoPlayVideoSwitch.isChecked = autoPlay
    }

    override fun setThemeMode(themeMode: ThemeMode) {
        when (themeMode) {
            ThemeMode.LIGHT -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }

            ThemeMode.DARK -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
        }
    }

    override fun clearCache() {
        context?.cacheDir?.path?.let { File(it).deleteRecursively() }
        showClearCacheSuccess()
    }

}
