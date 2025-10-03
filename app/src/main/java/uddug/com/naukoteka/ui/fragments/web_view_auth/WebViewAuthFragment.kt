package uddug.com.naukoteka.ui.fragments.web_view_auth

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Message
import android.view.View
import android.webkit.*
import androidx.core.os.bundleOf
import uddug.com.domain.repositories.auth.models.SocialType
import uddug.com.naukoteka.R
import uddug.com.naukoteka.databinding.FragmentWebViewBinding
import uddug.com.naukoteka.global.base.BaseFragment
import uddug.com.naukoteka.navigation.AppRouter
import uddug.com.naukoteka.navigation.Screens
import uddug.com.naukoteka.ui.AppConfigs
import uddug.com.naukoteka.ui.IntentKeys
import uddug.com.naukoteka.utils.BackButtonListener
import uddug.com.naukoteka.utils.viewBinding
import toothpick.ktp.delegate.inject

class WebViewAuthFragment : BaseFragment(R.layout.fragment_web_view), BackButtonListener {

    companion object {

        private const val URL_KEY = "WebViewAuthFragment.URL_KEY"

        fun newInstance(url: String) = WebViewAuthFragment().apply {
            arguments = bundleOf(URL_KEY to url)
        }
    }

    private val router: AppRouter by inject()

    private val requiredUrl get() = arguments?.getString(URL_KEY)

    override val contentView by viewBinding(FragmentWebViewBinding::bind)

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        clearCookies()
        contentView.run {
            webView.run {
                loadUrl(requiredUrl!!)
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.userAgentString = "Chrome/56.0.0.0 Mobile";
                settings.setSupportMultipleWindows(true)
                setAcceptThirdPartyCookies(this)
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(
                        view: WebView?,
                        request: WebResourceRequest?
                    ): Boolean {
                        if (request?.url.toString().contains(AppConfigs.SOCIAL_LOGIN_ENDPOINT)) {
                            val key = request?.url?.getQueryParameter("key")
                            val socialTypeRaw = request?.url?.getQueryParameter("social_type")
                            val socialType = SocialType.values().find { it.raw == socialTypeRaw }
                            if (key != null && socialType != null) {
                                router.sendResult(
                                    Screens.RESULT_AUTH_SOCIAL,
                                    IntentKeys.SocialAuthorization(key, socialType)
                                )
                                onBackPressed()
                            }
                            return false
                        }
                        request?.url
                        return super.shouldOverrideUrlLoading(view, request)
                    }

                    override fun shouldInterceptRequest(
                        view: WebView?,
                        request: WebResourceRequest?
                    ): WebResourceResponse? {
                        request?.url
                        return super.shouldInterceptRequest(view, request)
                    }
                }
                webChromeClient = object : WebChromeClient() {
                    override fun onCreateWindow(
                        view: WebView?,
                        isDialog: Boolean,
                        isUserGesture: Boolean,
                        resultMsg: Message?
                    ): Boolean {
                        return super.onCreateWindow(view, isDialog, isUserGesture, resultMsg)
                    }
                }
            }
        }
    }

    private fun setAcceptThirdPartyCookies(webView: WebView) {
        val cookieManager = CookieManager.getInstance()

        
        if (!cookieManager.acceptCookie()) {
            cookieManager.setAcceptCookie(true)
        }
        cookieManager.setAcceptThirdPartyCookies(webView, true)
    }


    private fun clearCookies() {
        val cookieManager = CookieManager.getInstance()
        cookieManager.removeAllCookies(null)
    }

    override fun onBackPressed(): Boolean {
        router.exit()
        return true
    }

}
