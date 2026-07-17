package uddug.com.naukoteka.presentation.profile.edit

import android.annotation.SuppressLint
import io.reactivex.Completable
import io.reactivex.disposables.CompositeDisposable
import moxy.InjectViewState
import toothpick.InjectConstructor
import uddug.com.domain.entities.profile.UserProfileFullInfo
import uddug.com.domain.interactors.user_profile.UserProfileInteractor
import uddug.com.naukoteka.global.base.BasePresenterImpl
import uddug.com.naukoteka.utils.text.isNotNullOrEmpty

@InjectConstructor
@InjectViewState
class ProfileEditPersonalIdsPresenter(
    private val userProfileInteractor: UserProfileInteractor
) : BasePresenterImpl<ProfileEditPersonalIdsView>() {

    companion object {
        private const val spinCodeSystemId = 3100
        private const val orchidSystemId = 3097
        private const val researcherSystemId = 3098
    }

    private val compositeDisposable = CompositeDisposable()

    var userProfileFullInfo: UserProfileFullInfo? = null

    fun setProfileFullInfo(profileFullInfo: UserProfileFullInfo) {
        this.userProfileFullInfo = profileFullInfo
        val identifiers = profileFullInfo.authors?.firstOrNull()?.identifiers
        identifiers?.find { it.cIdentSystemItem?.id == spinCodeSystemId }
            ?.cIdentSystemItem?.identifier?.let { viewState.setSpinCode(it) }
        identifiers?.find { it.cIdentSystemItem?.id == orchidSystemId }
            ?.cIdentSystemItem?.identifier?.let { viewState.setOrchid(it) }
        identifiers?.find { it.cIdentSystemItem?.id == researcherSystemId }
            ?.cIdentSystemItem?.identifier?.let { viewState.setReserch(it) }
        viewState.setMainInformation(profileFullInfo)
    }

    fun setCurrentSpinCode(spinCode: String) {
        findIdentifier(spinCodeSystemId)?.cIdentSystemItem?.identifier = spinCode
    }

    fun setCurrentOrchid(orchid: String) {
        findIdentifier(orchidSystemId)?.cIdentSystemItem?.identifier = orchid
    }

    fun setCurrentReserchId(researcherId: String) {
        findIdentifier(researcherSystemId)?.cIdentSystemItem?.identifier = researcherId
    }

    fun selectUpdateUserIds() {
        val userId = userProfileFullInfo?.id ?: return
        val author = userProfileFullInfo?.authors?.first() ?: return
        val spinCode = author.identifiers.find { it.cIdentSystemItem?.id == spinCodeSystemId }
        val orchid = author.identifiers.find { it.cIdentSystemItem?.id == orchidSystemId }
        val researcher = author.identifiers.find { it.cIdentSystemItem?.id == researcherSystemId }

        fun updateId(id: String, identifier: String, rObject: String, cIdentSystem: String) =
            userProfileInteractor.updateUserObjectId(userId, id, identifier, rObject, cIdentSystem)

        userProfileInteractor.updateUserObjectId(
            userId,
            spinCode?.id.orEmpty(),
            spinCode?.cIdentSystemItem?.identifier.orEmpty(),
            spinCode?.rObject.orEmpty(),
            spinCode?.cIdentSystem.orEmpty()
        ).andThen(
            if (orchid?.cIdentSystemItem?.identifier.isNotNullOrEmpty())
                updateId(orchid?.id.orEmpty(), orchid?.cIdentSystemItem?.identifier.orEmpty(), orchid?.rObject.orEmpty(), orchid?.cIdentSystem.orEmpty())
            else Completable.fromAction {}
        ).andThen(
            if (researcher?.cIdentSystemItem?.identifier.isNotNullOrEmpty())
                updateId(researcher?.id.orEmpty(), researcher?.cIdentSystemItem?.identifier.orEmpty(), researcher?.rObject.orEmpty(), researcher?.cIdentSystem.orEmpty())
            else Completable.fromAction {}
        ).subscribe({
            viewState.showIdsUpdatedSuccess()
        }, {
            it.printStackTrace()
        })
    }

    private fun findIdentifier(systemId: Int) =
        userProfileFullInfo?.authors?.firstOrNull()?.identifiers?.find { it.cIdentSystemItem?.id == systemId }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }
}
