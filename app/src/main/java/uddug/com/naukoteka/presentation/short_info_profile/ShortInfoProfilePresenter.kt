package uddug.com.naukoteka.presentation.short_info_profile

import io.reactivex.subjects.BehaviorSubject
import uddug.com.data.validator.FieldsValidator
import uddug.com.domain.interactors.user_profile.UserProfileInteractor
import uddug.com.domain.interactors.user_profile.model.ShortInfoUi
import uddug.com.naukoteka.global.base.BasePresenterImpl
import uddug.com.naukoteka.navigation.AppRouter
import uddug.com.naukoteka.navigation.Screens
import moxy.InjectViewState
import toothpick.InjectConstructor
import java.util.concurrent.TimeUnit

@InjectConstructor
@InjectViewState
class ShortInfoProfilePresenter(
    private val fieldsValidator: FieldsValidator,
    val router: AppRouter,
    private val userProfileInteractor: UserProfileInteractor
) : BasePresenterImpl<ShortInfoProfileView>() {

    private var isAvailableNickname = false
    private var defaultNickname: String? = null

    private val inputNicknameSubject = BehaviorSubject.create<String>()

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

    fun isValidFields(surname: String, name: String, patronymic: String, withoutPatronymic : Boolean, link: String) {
        viewState.showButtonState(
            fieldsValidator.isNotEmpty(surname) &&
                    fieldsValidator.isNotEmpty(name) &&
                    (withoutPatronymic || fieldsValidator.isNotEmpty(patronymic)) &&
                    fieldsValidator.isNotEmpty(link) &&
                    isAvailableNickname
        )
    }

    fun checkFreeNickname(nickname: String) {
        if (nickname == defaultNickname) {
            isAvailableNickname = true
            viewState.showNicknameAvailable(isAvailableNickname)
        } else {
            inputNicknameSubject.onNext(nickname)
        }
    }

    fun fillProfile(shortInfoUi: ShortInfoUi) {
        userProfileInteractor.setUser(shortInfoUi)
            .await {
                viewState.showInfoMessage("Данные заполнены успешно")
                router.newRootScreen(Screens.Login())
            }
    }
}
