package uddug.com.naukoteka.presentation.profile.edit

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

    private var isNicknameAvailable = false
    private var defaultNickname: String? = null

    private val nicknameInputSubject = BehaviorSubject.create<String>()

    private val compositeDisposable = CompositeDisposable()

    var userProfileFullInfo: UserProfileFullInfo? = null

    fun setProfileFullInfo(profileFullInfo: UserProfileFullInfo) {
        this.userProfileFullInfo = profileFullInfo
        viewState.setMainInformation(profileFullInfo)
    }

    fun updateCurrentUserId(userId: String) {
        if (!isNicknameAvailable) return
        userProfileFullInfo?.let { profile ->
            profile.nickname = userId
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

    fun checkFreeNickname(nickname: String) {
        if (nickname == defaultNickname) {
            isNicknameAvailable = true
            viewState.showNicknameAvailable(true)
        } else {
            nicknameInputSubject.onNext(nickname)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        startListeningNicknameChanges()
    }

    private fun startListeningNicknameChanges() {
        nicknameInputSubject.debounce(500L, TimeUnit.MILLISECONDS)
            .switchMap { nickname -> userProfileInteractor.checkNickname(nickname) }
            .subscribe({ available ->
                isNicknameAvailable = available
                viewState.showNicknameAvailable(available)
            }, this::onError)
            .connect()
    }
}
