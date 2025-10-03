package uddug.com.naukoteka.ui.fragments.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import uddug.com.data.cache.first_launch.FirstLaunchCache
import uddug.com.naukoteka.R
import uddug.com.naukoteka.databinding.FragmentSplashBinding
import uddug.com.naukoteka.global.base.BaseFragment
import uddug.com.naukoteka.navigation.AppRouter
import uddug.com.naukoteka.navigation.Screens
import uddug.com.naukoteka.utils.viewBinding
import toothpick.ktp.delegate.inject
import uddug.com.data.NaukotekaCookieJar
import uddug.com.data.cache.cookies.CookiesCache
import uddug.com.naukoteka.presentation.profile.ProfileAvatarActionPresenter
import uddug.com.naukoteka.ui.activities.main.ContainerActivity
import uddug.com.naukoteka.ui.fragments.profile.settings.ThemeMode
import uddug.com.naukoteka.utils.text.isNotNullOrEmpty

class SplashFragment : BaseFragment(R.layout.fragment_splash), SplashView {

    override val contentView by viewBinding(FragmentSplashBinding::bind)

    private val router: AppRouter by inject()
    private val isFirstLaunched: FirstLaunchCache by inject()
    private val cookiesCache: CookiesCache by inject()

    @InjectPresenter
    lateinit var presenter: SplashPresenter

    @ProvidePresenter
    fun providePresenter(): SplashPresenter {
        return getScope().getInstance(SplashPresenter::class.java)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)










    }

    override fun showContainerActivity() {
        startActivity(Intent(requireContext(), ContainerActivity::class.java))
    }

    override fun showAuthActivity() {
        router.newRootScreen(Screens.Tutorial())
    }

    override fun showUserTheme(userTheme: UserTheme) {
        when (userTheme) {
            UserTheme.LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            UserTheme.DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
    }

}
