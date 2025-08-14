package uddug.com.naukoteka.ui.fragments.settings

import io.reactivex.disposables.CompositeDisposable
import moxy.InjectViewState
import toothpick.InjectConstructor
import uddug.com.domain.entities.country.Settlement
import uddug.com.domain.entities.profile.FormContainer
import uddug.com.domain.interactors.country.LocationInteractor

import uddug.com.domain.entities.profile.UserProfileFullInfo
import uddug.com.domain.interactors.user_profile.UserProfileInteractor

import uddug.com.naukoteka.global.base.BasePresenterImpl
import uddug.com.naukoteka.presentation.profile.edit.models.SettlementType
import uddug.com.naukoteka.utils.text.isNotNullOrEmpty
import java.util.Locale


@InjectConstructor
@InjectViewState
class AppSettingsPresenter(
    private val locationInteractor: LocationInteractor,
    private val userProfileInteractor: UserProfileInteractor
) : BasePresenterImpl<AppSettingsView>() {

    var compositeDisposable: CompositeDisposable = CompositeDisposable()

    var userProfileFullInfo: UserProfileFullInfo? = null


    fun setProfileFullInfo(profileFullInfo: UserProfileFullInfo) {
        this.userProfileFullInfo = profileFullInfo
        compositeDisposable.addAll(
            userProfileInteractor.getUserSettings()
                .subscribe({ forms ->
                    viewState.setUserDynamicNavigationForms(forms.forms.filter { it.elements?.isNotEmpty() == true })
                }, {
                    it.printStackTrace()
                })
        )
    }

    fun askForOpenAppSettings() {
        userProfileFullInfo?.let {
            viewState.openProfileSecure(
                profileInfo = it
            )
        }
    }

    fun askForOpenSystemSettings() {
        userProfileFullInfo?.let {
            viewState.openSystemSettings(
                profileInfo = it
            )
        }
    }
    fun selectNavigationContainer(formContainer: FormContainer) {
        userProfileFullInfo?.let {
            viewState.openDynamicNavigationContainer(
                formContainer,
                it
            )
        }
    }
}
