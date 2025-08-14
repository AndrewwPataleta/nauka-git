package uddug.com.naukoteka.presentation.signed_up

import uddug.com.naukoteka.global.base.BasePresenterImpl
import uddug.com.naukoteka.navigation.AppRouter
import uddug.com.naukoteka.navigation.Screens
import moxy.InjectViewState
import toothpick.InjectConstructor

@InjectConstructor
@InjectViewState
class SignedUpFinishedPresenter(
    val router: AppRouter
) : BasePresenterImpl<SignedUpView>() {


    fun fillProfile() {
        router.navigateTo(Screens.ShortInfoProfile())
    }

    fun skip() {
        router.newRootScreen(Screens.Login())
    }

}