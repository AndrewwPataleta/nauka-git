package uddug.com.naukoteka.presentation.settings

import android.util.Log
import uddug.com.domain.interactors.user_profile.UserProfileInteractor
import uddug.com.naukoteka.global.base.BasePresenterImpl
import moxy.InjectViewState
import toothpick.InjectConstructor
import uddug.com.domain.entities.profile.UserProfileFullInfo

@InjectConstructor
@InjectViewState
class AppSettingsPresenter(

) : BasePresenterImpl<AppSettingsView>() {

    companion object {
        private const val errorTag = "ProfilePresenterError"
    }

    var userProfileFullInfo: UserProfileFullInfo? = null


}
