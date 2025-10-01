package uddug.com.naukoteka.ui.activities.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.LocaleChangerAppCompatDelegate
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.franmontiel.localechanger.LocaleChanger
import com.github.terrakok.cicerone.Navigator
import com.github.terrakok.cicerone.NavigatorHolder
import uddug.com.naukoteka.R
import uddug.com.naukoteka.databinding.ActivityMainBinding
import uddug.com.naukoteka.global.base.BaseActivity
import uddug.com.naukoteka.navigation.AnimatableAppNavigator
import uddug.com.naukoteka.navigation.AppRouter
import uddug.com.naukoteka.navigation.Screens
import uddug.com.naukoteka.navigation.Screens.Splash
import uddug.com.naukoteka.presentation.main.MainPresenter
import uddug.com.naukoteka.presentation.main.MainView
import uddug.com.naukoteka.ui.IntentKeys
import uddug.com.naukoteka.ui.custom.square_toast.SquareToast
import uddug.com.naukoteka.ui.custom.square_toast.ToastAction
import uddug.com.naukoteka.ui.custom.square_toast.ToastInfo
import uddug.com.naukoteka.ui.fragments.password_recovery.PasswordRecoveryFragment
import uddug.com.naukoteka.ui.fragments.registration_third_step.RegistrationThirdStepFragment
import uddug.com.naukoteka.utils.ActivityResultListener
import uddug.com.naukoteka.utils.BackButtonListener
import uddug.com.naukoteka.utils.RouterProvider
import uddug.com.naukoteka.utils.ui.findFragment
import uddug.com.naukoteka.utils.viewBinding
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import toothpick.ktp.delegate.inject
import uddug.com.naukoteka.databinding.ActivityAuthBinding
import uddug.com.naukoteka.navigation.Screens.Login
import uddug.com.naukoteka.navigation.Screens.TO_LOGIN

class AuthActivity : BaseActivity(), RouterProvider, MainView {

    override val contentView by viewBinding(ActivityAuthBinding::inflate)

    private val navigatorHolder: NavigatorHolder by inject()

    private val navigator: Navigator by lazy { AnimatableAppNavigator(this, R.id.container) }

    @InjectPresenter
    lateinit var presenter: MainPresenter

    @ProvidePresenter
    fun providePresenter(): MainPresenter {
        return getScope().getInstance(MainPresenter::class.java)
    }

    protected lateinit var progressBar: ProgressBar

    override fun onResumeFragments() {
        super.onResumeFragments()
        navigatorHolder.setNavigator(navigator)
    }

    override fun onPause() {
        navigatorHolder.removeNavigator()
        super.onPause()
    }

    override val router: AppRouter by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(contentView.root)
        val isLaunchedAfterSystemKilled = savedInstanceState != null
        if (!isLaunchedAfterSystemKilled) {
            router.navigateTo(Splash())
            
            
        }

        LayoutInflater.from(this).inflate(R.layout.progress_bar, contentView.container, true)
            .also { view ->
                this.progressBar = view.findViewById(R.id.progressBar) as ProgressBar
            }
    }

    private fun handleDeepLink(newIntent: Intent) {
        newIntent.getParcelableExtra<IntentKeys.Registration>(IntentKeys.Registration.KEY)
            ?.let { registration ->
                findFragment<RegistrationThirdStepFragment>(Screens.REGISTRATION_THIRD_STEP)
                    ?.checkTokenToRegistration(registration.key)
            }
        newIntent.getParcelableExtra<IntentKeys.RecoveryPassword>(IntentKeys.RecoveryPassword.KEY)
            ?.let { recovery ->
                findFragment<PasswordRecoveryFragment>(Screens.PASSWORD_RECOVERY)
                    ?.checkTokenToRecovery(recovery.key)
            }
        newIntent.getParcelableExtra<IntentKeys.SocialAuthorization>(IntentKeys.SocialAuthorization.KEY)
            ?.let { socialAuth ->
                presenter.socialAuth(socialAuth.socialType, socialAuth.key)
                
                
            }

        if (newIntent.getBooleanExtra(TO_LOGIN, false)) {
            router.navigateTo(Login())
        }

    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDeepLink(intent)
    }

    override fun onBackPressed() {
        val fm = supportFragmentManager
        var fragment: Fragment? = null
        val fragments = fm.fragments
        for (f in fragments) {
            if (f.isVisible) {
                fragment = f
                break
            }
        }
        if (fragment != null && fragment is BackButtonListener
            && (fragment as BackButtonListener).onBackPressed()
        ) {
            return
        } else {
            router.exit()
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (currentFocus != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val fm = supportFragmentManager
        var fragment: Fragment? = null
        val fragments = fm.fragments
        for (f in fragments) {
            if (f.isVisible) {
                fragment = f
                break
            }
        }
        if (fragment != null && fragment is ActivityResultListener) {
            (fragment as ActivityResultListener).onFragmentResult(requestCode, resultCode, data)
        }
    }

    override fun showLoading(show: Boolean) {
        runOnUiThread {
            progressBar.isVisible = show
        }
    }

    override fun showRefreshLoading(show: Boolean) {

    }

    override fun getDelegate(): AppCompatDelegate {
        return LocaleChangerAppCompatDelegate(super.getDelegate())
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(LocaleChanger.configureBaseContext(newBase))
    }

    override fun showInfoMessage(message: Int?) {
        if (message == null) return
        showInfoMessage(getString(message))
    }

    override fun showInfoMessage(message: String?) {
        if (message.isNullOrBlank()) return
        SquareToast.getInstance(this).show(ToastInfo(text = message))
    }

    override fun showActionMessage(message: Int?, action: Int, invoker: () -> Unit) {
        if (message == null) return
        showActionMessage(getString(message), getString(action), invoker)
    }

    override fun showActionMessage(message: String?, action: String, invoker: () -> Unit) {
        if (message.isNullOrBlank()) return
        SquareToast.getInstance(this).show(
            ToastInfo(
                text = message,
                action = ToastAction(action, invoker)
            )
        )
    }

    override fun showMessage(data: ToastInfo) {
        SquareToast.getInstance(this).show(data)
    }

}
