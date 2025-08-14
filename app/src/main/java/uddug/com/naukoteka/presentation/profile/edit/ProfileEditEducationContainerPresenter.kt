package uddug.com.naukoteka.presentation.profile.edit

import android.annotation.SuppressLint
import android.util.Log
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import moxy.InjectViewState
import toothpick.InjectConstructor
import uddug.com.domain.entities.profile.UserProfileFullInfo
import uddug.com.domain.interactors.user_profile.UserProfileInteractor
import uddug.com.naukoteka.global.base.BasePresenterImpl
import uddug.com.naukoteka.presentation.profile.ProfilePresenter
import uddug.com.naukoteka.presentation.profile.ProfilePresenter.Companion
import java.util.concurrent.TimeUnit


@InjectConstructor
@InjectViewState
class ProfileEditEducationContainerPresenter(
    private val userProfileInteractor: UserProfileInteractor
) : BasePresenterImpl<ProfileEditEducationContainerView>() {

    companion object {
        private const val errorTag = "ProfileEditPlacementViewError"
    }

    var compositeDisposable: CompositeDisposable = CompositeDisposable()

    var userProfileFullInfo: UserProfileFullInfo? = null

    fun setProfileFullInfo(profileFullInfo: UserProfileFullInfo) {
        this.userProfileFullInfo = profileFullInfo
        viewState.setMainInformation(
            profileFullInfo
        )
    }

    fun loadProfile() {
        compositeDisposable.add(
            userProfileInteractor.getUserProfilePreviewInfo().subscribe({
                userProfileFullInfo = it
                viewState.setMainInformation(it)
            }, {

            })
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
    }


}
