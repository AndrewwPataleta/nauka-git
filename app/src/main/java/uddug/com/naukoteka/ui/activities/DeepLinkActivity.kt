package uddug.com.naukoteka.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import uddug.com.domain.repositories.auth.models.SocialType
import uddug.com.naukoteka.R
import uddug.com.naukoteka.databinding.ActivityAuthBinding
import uddug.com.naukoteka.databinding.ActivityMainBinding
import uddug.com.naukoteka.global.base.BaseActivity
import uddug.com.naukoteka.ui.IntentKeys
import uddug.com.naukoteka.ui.activities.main.AuthActivity
import uddug.com.naukoteka.utils.ActivityResultListener
import uddug.com.naukoteka.utils.viewBinding

class DeepLinkActivity : BaseActivity() {

    override val contentView by viewBinding(ActivityAuthBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(contentView.root)

        handleDeepLink()

    }

    private fun handleDeepLink(newIntent: Intent? = null) {
        val intent = newIntent ?: intent
        val keyAuthorization = intent.data?.getQueryParameter("key")
        if (intent.dataString?.contains(getString(R.string.deep_link_verify_email)) == true) {
            if (!keyAuthorization.isNullOrEmpty()) {
                startActivity(Intent(this, AuthActivity::class.java).apply {
                    putExtra(IntentKeys.Registration.KEY, IntentKeys.Registration(keyAuthorization))
                })
            }
        } else if (intent.dataString?.contains(getString(R.string.deep_link_social_login)) == true) {
            if (!keyAuthorization.isNullOrEmpty()) {
                val socialTypeRaw = intent.data?.getQueryParameter("social_type")
                val socialType = SocialType.values().find { it.raw == socialTypeRaw }
                startActivity(Intent(this, AuthActivity::class.java).apply {
                    putExtra(
                        IntentKeys.SocialAuthorization.KEY,
                        IntentKeys.SocialAuthorization(keyAuthorization, socialType!!)
                    )
                })
            }
        } else if (intent.dataString?.contains(getString(R.string.deep_link_reset_credentials)) == true) {
            if (!keyAuthorization.isNullOrEmpty()) {
                startActivity(Intent(this, AuthActivity::class.java).apply {
                    putExtra(
                        IntentKeys.RecoveryPassword.KEY,
                        IntentKeys.RecoveryPassword(keyAuthorization)
                    )
                })
            }
        }
        finish()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDeepLink(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
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


}
