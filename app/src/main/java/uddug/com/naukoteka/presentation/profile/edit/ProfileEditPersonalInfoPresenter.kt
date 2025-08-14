package uddug.com.naukoteka.presentation.profile.edit

import android.annotation.SuppressLint
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import moxy.InjectViewState
import toothpick.InjectConstructor
import uddug.com.domain.entities.profile.UserProfileFullInfo
import uddug.com.domain.interactors.user_profile.UserProfileInteractor
import uddug.com.domain.interactors.user_profile.model.ShortInfoUpdate
import uddug.com.naukoteka.global.base.BasePresenterImpl
import java.util.concurrent.TimeUnit


@InjectConstructor
@InjectViewState
class ProfileEditPersonalInfoPresenter(
    private val userProfileInteractor: UserProfileInteractor
) : BasePresenterImpl<ProfileEditPersonalInfoView>() {

    companion object {
        private const val errorTag = "ProfileEditPresenterError"
        const val MAX_DEFAULT_INPUT = 30
        const val MAX_DESCRIPTION = 500
        var genres = arrayOf("Мужчина", "Женщина")
        val NEW_SPINNER_ID = 1
        val UREF_MAN = "46:2"
        val UREF_WOMAN = "46:1"
    }


    var compositeDisposable: CompositeDisposable = CompositeDisposable()

    var userProfileFullInfo: UserProfileFullInfo? = null

    fun setProfileFullInfo(profileFullInfo: UserProfileFullInfo) {
        this.userProfileFullInfo = profileFullInfo
        viewState.setMainInformation(profileFullInfo)
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }

    fun setFirstName(firstName: String) {
        userProfileFullInfo?.firstName = firstName
        updateLengthFields()
    }

    fun setSecondName(secondName: String) {
        userProfileFullInfo?.lastName = secondName
        updateLengthFields()
    }

    fun setMiddleName(middleName: String) {
        userProfileFullInfo?.middleName = middleName
        updateLengthFields()
    }

    fun setDescription(dsc: String) {
        userProfileFullInfo?.dsc = dsc
        updateLengthFields()
    }

    fun searchForInterestsByQuery(query: String) {

    }

    fun setBirthday(birthday: String) {
        userProfileFullInfo?.birthDate = birthday

    }

    private fun updateLengthFields() {
        viewState.setMaxInputRange(
            maxDefault = MAX_DEFAULT_INPUT,
            maxDescription = MAX_DESCRIPTION
        )
        viewState.updateLengthInputs(
            maxDefault = MAX_DEFAULT_INPUT,
            maxDescription = MAX_DESCRIPTION
        )
    }

    fun setGenderPosition(position: Int) {
        userProfileFullInfo?.gender = when (position) {
            0 -> UREF_MAN
            1 -> UREF_WOMAN
            else -> UREF_MAN
        }
    }

    fun updateProfileShortInfo() {
        compositeDisposable.add(
            userProfileInteractor.updateProfileShortInfo(
                shortInfoUpdate = ShortInfoUpdate(
                    id = userProfileFullInfo?.id ?: "",
                    name = userProfileFullInfo?.firstName,
                    surname = userProfileFullInfo?.lastName,
                    middleName = userProfileFullInfo?.middleName,
                    description = userProfileFullInfo?.dsc,
                    birthday = userProfileFullInfo?.birthDate,
                    nickname = userProfileFullInfo?.nickname ?: "",
                    gender = userProfileFullInfo?.gender
                )
            ).subscribe({
                viewState.profileSuccessfulUpdate()
            }, {})
        )
    }

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        viewState.setGenders(
            genres.toList(), selectedPos = when (userProfileFullInfo?.gender) {
                UREF_MAN -> 0
                UREF_WOMAN -> 1
                else -> -1
            }
        )
        updateLengthFields()
    }


}
