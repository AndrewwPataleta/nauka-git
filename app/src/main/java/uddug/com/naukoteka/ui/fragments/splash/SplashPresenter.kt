package uddug.com.naukoteka.ui.fragments.splash

import android.util.Log
import io.reactivex.disposables.CompositeDisposable
import moxy.InjectViewState
import toothpick.InjectConstructor
import uddug.com.data.cache.cookies.CookiesCache
import uddug.com.data.cache.model.UserTheme
import uddug.com.data.cache.system_settings.UserSystemSettingsCache
import uddug.com.domain.entities.profile.UserProfileFullInfo
import uddug.com.domain.interactors.user_profile.UserProfileInteractor
import uddug.com.domain.interactors.user_profile.model.ShortInfoUpdate
import uddug.com.naukoteka.global.base.BasePresenterImpl

@InjectConstructor
@InjectViewState
class SplashPresenter(
    private val userProfileInteractor: UserProfileInteractor,
    private val userSystemSettingsCache: UserSystemSettingsCache,
    private val cookiesCache: CookiesCache
) : BasePresenterImpl<SplashView>() {


    var compositeDisposable: CompositeDisposable = CompositeDisposable()

    init {
        when (userSystemSettingsCache.entity?.theme) {
            UserTheme.LIGHT -> viewState.showUserTheme(uddug.com.naukoteka.ui.fragments.splash.UserTheme.LIGHT)
            UserTheme.DARK -> viewState.showUserTheme(uddug.com.naukoteka.ui.fragments.splash.UserTheme.DARK)
            null -> Unit
        }
        compositeDisposable.add(
            userProfileInteractor.validateUser().subscribe({
                viewState.showContainerActivity()
            }, {
                viewState.showAuthActivity()
            })
        )
    }

}
