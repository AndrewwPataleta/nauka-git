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
class DynamicSettingsPresenter(
    private val locationInteractor: LocationInteractor,
    private val userProfileInteractor: UserProfileInteractor
) : BasePresenterImpl<DynamicSettingsView>() {

    var compositeDisposable: CompositeDisposable = CompositeDisposable()

    var userProfileFullInfo: UserProfileFullInfo? = null

    var userFormContainer: FormContainer? = null


    fun setProfileFullInfo(profileFullInfo: UserProfileFullInfo) {
        this.userProfileFullInfo = profileFullInfo
    }

    fun setUserForm(userFormContainer: FormContainer) {

        this.userFormContainer = userFormContainer
        userFormContainer.elements?.first()?.cls?.let {
            userProfileInteractor.getUserCls(cls = it).subscribe({
                userProfileFullInfo?.settings?.let { it1 ->
                    viewState.setDynamicSettingsForm(
                        userFormContainer, it,
                        it1
                    )
                }
            }, {})
        }
    }

    fun selectUpdateCurrentSettings() {
        userProfileFullInfo?.settings?.let {
            userProfileInteractor.updateCurrentSettings(
                settings = it
            ).subscribe({
                viewState.showToastUpdate(ToastStatus.SUCCESS)
            }, {
                viewState.showToastUpdate(ToastStatus.FAIL)
            })
        }
    }

}
