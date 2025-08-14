package uddug.com.naukoteka.presentation.profile.edit

import android.annotation.SuppressLint
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import moxy.InjectViewState
import toothpick.InjectConstructor
import uddug.com.domain.entities.profile.UserProfileFullInfo
import uddug.com.domain.interactors.user_profile.UserProfileInteractor
import uddug.com.naukoteka.global.base.BasePresenterImpl
import java.util.concurrent.TimeUnit


@InjectConstructor
@InjectViewState
class ProfileEditIdPresenter(
    private val userProfileInteractor: UserProfileInteractor
) : BasePresenterImpl<ProfileEditIdView>() {

    companion object {
        private const val errorTag = "ProfileEditPresenterError"
    }

    private var isAvailableNickname = false
    private var defaultNickname: String? = null

    private val inputNicknameSubject = BehaviorSubject.create<String>()

    var compositeDisposable: CompositeDisposable = CompositeDisposable()

    var userProfileFullInfo: UserProfileFullInfo? = null

    fun setProfileFullInfo(profileFullInfo: UserProfileFullInfo) {
        this.userProfileFullInfo = profileFullInfo
        viewState.setMainInformation(profileFullInfo)
    }

    fun updateCurrentUserId(userId: String) {
        if (isAvailableNickname) {
            userProfileFullInfo?.let { profile ->
                userProfileFullInfo?.nickname = userId
                compositeDisposable.add(
                    userProfileInteractor.updateUserId(
                        id = profile.id ?: "",
                        nickname = userId,
                        firstname = profile.firstName ?: "",
                        lastname = profile.lastName ?: ""
                    ).subscribe({
                        viewState.showUpdatedDone()
                    }, {})
                )
            }

        }
    }

    fun checkFreeNickname(nickname: String) {
        if (nickname == defaultNickname) {
            isAvailableNickname = true
            viewState.showNicknameAvailable(isAvailableNickname)
        } else {
            inputNicknameSubject.onNext(nickname)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        startListenNicknameChanges()
    }

    private fun startListenNicknameChanges() {
        inputNicknameSubject.debounce(500L, TimeUnit.MILLISECONDS)
            .switchMap { nickname -> userProfileInteractor.checkNickname(nickname) }
            .subscribe({ nickNameAvailable ->
                isAvailableNickname = nickNameAvailable
                viewState.showNicknameAvailable(nickNameAvailable)
            }, this::onError)
            .connect()
    }

}
