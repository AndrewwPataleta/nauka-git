package uddug.com.naukoteka.global.base

import com.github.terrakok.cicerone.Router
import moxy.MvpView
import uddug.com.domain.interactors.auth.AuthInteractor
import uddug.com.domain.repositories.auth.models.SocialType
import uddug.com.naukoteka.global.views.InformativeView
import uddug.com.naukoteka.global.views.LoadingView
import uddug.com.naukoteka.navigation.Screens
import uddug.com.naukoteka.ui.AppConfigs
import uddug.com.naukoteka.ui.IntentKeys
import uddug.com.naukoteka.ui.custom.square_toast.SquareToast
import uddug.com.naukoteka.ui.custom.square_toast.ToastInfo


interface SocialLoginView : MvpView, InformativeView, LoadingView

abstract class SocialLoginPresenter<V : SocialLoginView> : BasePresenterImpl<V>() {

    abstract val router: Router
    abstract val authInteractor: AuthInteractor

    fun authSocial(socialType: SocialType) {
        router.run {
            val url = AppConfigs.authSocialUrl(socialType)
            
            
            
                setResultListener(Screens.RESULT_AUTH_SOCIAL) {
                    (it as? IntentKeys.SocialAuthorization)?.let { socialAuthorization ->
                        socialAuth(socialAuthorization.socialType, socialAuthorization.key)
                    }
                }
                navigateTo(Screens.WebViewAuth(url))
            
        }
    }

    fun socialAuth(socialType: SocialType, key: String) {
        authInteractor.authenticate(socialType, key)
            .await {
                if (viewState is LoadingView) {
                    viewState.showMessage(ToastInfo("Success", type = SquareToast.Type.SUCCESS))
                }
            }
    }


}
