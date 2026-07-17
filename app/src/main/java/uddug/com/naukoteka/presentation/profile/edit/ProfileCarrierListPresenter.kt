package uddug.com.naukoteka.presentation.profile.edit

import io.reactivex.disposables.CompositeDisposable
import moxy.InjectViewState
import toothpick.InjectConstructor
import uddug.com.domain.entities.profile.LaborActivities
import uddug.com.domain.entities.profile.UserProfileFullInfo
import uddug.com.domain.interactors.user_profile.UserProfileInteractor
import uddug.com.naukoteka.global.base.BasePresenterImpl

@InjectConstructor
@InjectViewState
class ProfileCarrierListPresenter(
    private val userProfileInteractor: UserProfileInteractor
) : BasePresenterImpl<ProfileCarrierListView>() {

    private val compositeDisposable = CompositeDisposable()

    var userProfileFullInfo: UserProfileFullInfo? = null

    fun setProfileFullInfo(profileFullInfo: UserProfileFullInfo) {
        this.userProfileFullInfo = profileFullInfo
        viewState.setCarrierItems(profileFullInfo.laborActivity)
    }

    fun loadProfile() {
        compositeDisposable.add(
            userProfileInteractor.getUserProfilePreviewInfo().subscribe({ profile ->
                userProfileFullInfo = profile
                viewState.setCarrierItems(profile.laborActivity)
            }, {})
        )
    }

    fun askForDeleteItem(laborActivities: LaborActivities) {
        viewState.showDeleteDialog(laborActivities)
    }

    fun askForDetailInfoItem(laborActivities: LaborActivities) {
        userProfileFullInfo?.let { viewState.showDetailScreen(it, laborActivities.id) }
    }

    fun askForCreateEducation() {
        userProfileFullInfo?.let { viewState.showDetailScreen(it, null) }
    }

    fun confirmDeleteLaborActivities(laborActivities: LaborActivities) {
        userProfileFullInfo?.id?.let { userId ->
            compositeDisposable.add(
                userProfileInteractor.removeUserLaborActivity(userId = userId, laborActivities).subscribe({
                    loadProfile()
                }, {})
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }
}
