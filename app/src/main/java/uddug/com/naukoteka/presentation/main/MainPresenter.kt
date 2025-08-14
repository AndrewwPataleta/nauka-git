package uddug.com.naukoteka.presentation.main

import uddug.com.domain.interactors.auth.AuthInteractor
import uddug.com.domain.repositories.auth.models.SocialType
import uddug.com.naukoteka.global.base.BasePresenterImpl
import uddug.com.naukoteka.ui.custom.square_toast.SquareToast
import uddug.com.naukoteka.ui.custom.square_toast.ToastInfo
import toothpick.InjectConstructor

@InjectConstructor
class MainPresenter(private val authInteractor: AuthInteractor) : BasePresenterImpl<MainView>() {


    fun socialAuth(socialType: SocialType, key: String) {
        authInteractor.authenticate(socialType, key)
            .await {
                viewState.showMessage(ToastInfo("Success", type = SquareToast.Type.SUCCESS))
            }
    }

}