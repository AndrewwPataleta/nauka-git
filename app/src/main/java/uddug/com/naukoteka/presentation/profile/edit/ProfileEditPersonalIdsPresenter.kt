package uddug.com.naukoteka.presentation.profile.edit

import android.annotation.SuppressLint
import io.reactivex.Completable
import io.reactivex.CompletableSource
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import moxy.InjectViewState
import toothpick.InjectConstructor
import uddug.com.domain.entities.profile.UserProfileFullInfo
import uddug.com.domain.interactors.user_profile.UserProfileInteractor
import uddug.com.naukoteka.global.base.BasePresenterImpl
import uddug.com.naukoteka.utils.text.isNotNullOrEmpty
import java.util.concurrent.TimeUnit


@InjectConstructor
@InjectViewState
class ProfileEditPersonalIdsPresenter(
    private val userProfileInteractor: UserProfileInteractor
) : BasePresenterImpl<ProfileEditPersonalIdsView>() {

    companion object {
        private const val errorTag = "ProfileEditPresenterError"
        private const val spinCodeId = 3100
        private const val orchidCodeId = 3097
        private const val resId = 3098
    }


    var compositeDisposable: CompositeDisposable = CompositeDisposable()

    var userProfileFullInfo: UserProfileFullInfo? = null

    fun setProfileFullInfo(profileFullInfo: UserProfileFullInfo) {
        this.userProfileFullInfo = profileFullInfo
        userProfileFullInfo?.authors?.firstOrNull()?.identifiers?.find {
            it.cIdentSystemItem?.id == spinCodeId
        }?.cIdentSystemItem?.identifier?.let {
            viewState.setSpinCode(
                it
            )
        }
        userProfileFullInfo?.authors?.firstOrNull()?.identifiers?.find {
            it.cIdentSystemItem?.id == orchidCodeId
        }?.cIdentSystemItem?.identifier?.let {
            viewState.setOrchid(
                it
            )
        }
        userProfileFullInfo?.authors?.firstOrNull()?.identifiers?.find {
            it.cIdentSystemItem?.id == resId
        }?.cIdentSystemItem?.identifier?.let {
            viewState.setReserch(
                it
            )
        }
        viewState.setMainInformation(profileFullInfo)
    }

    fun setCurrentSpinCode(spinCode: String) {
        userProfileFullInfo?.authors?.first().apply {
            this?.identifiers?.find {
                it.cIdentSystemItem?.id == spinCodeId
            }?.cIdentSystemItem?.identifier = spinCode
        }
    }

    fun setCurrentOrchid(orchid: String) {
        userProfileFullInfo?.authors?.first().apply {
            this?.identifiers?.find {
                it.cIdentSystemItem?.id == orchidCodeId
            }?.cIdentSystemItem?.identifier = orchid
        }
    }

    fun setCurrentReserchId(reserchedId: String) {
        userProfileFullInfo?.authors?.first().apply {
            this?.identifiers?.find {
                it.cIdentSystemItem?.id == resId
            }?.cIdentSystemItem?.identifier = reserchedId
        }
    }

    fun selectUpdateUserIds() {
        userProfileFullInfo?.id?.let {
            userProfileFullInfo?.authors?.first()?.let { it1 ->
                val spinCode = it1.identifiers.find { it.cIdentSystemItem?.id == spinCodeId }
                val orchidCodeId = it1.identifiers.find { it.cIdentSystemItem?.id == orchidCodeId }
                val reserchedId = it1.identifiers.find { it.cIdentSystemItem?.id == resId }
                userProfileInteractor.updateUserObjectId(
                    it,
                    spinCode?.id.orEmpty(),
                    spinCode?.cIdentSystemItem?.identifier.orEmpty(),
                    spinCode?.rObject.orEmpty(),
                    spinCode?.cIdentSystem.orEmpty()
                ).andThen(
                    if (orchidCodeId?.cIdentSystemItem?.identifier.isNotNullOrEmpty()) {
                        userProfileInteractor.updateUserObjectId(
                            it,
                            orchidCodeId?.id.orEmpty(),
                            orchidCodeId?.cIdentSystemItem?.identifier.orEmpty(),
                            orchidCodeId?.rObject.orEmpty(),
                            orchidCodeId?.cIdentSystem.orEmpty()
                        )
                    } else {
                        Completable.fromAction { }
                    }
                ).andThen(
                    if (reserchedId?.cIdentSystemItem?.identifier.isNotNullOrEmpty()) {
                        userProfileInteractor.updateUserObjectId(
                            it,
                            reserchedId?.id.orEmpty(),
                            reserchedId?.cIdentSystemItem?.identifier.orEmpty(),
                            reserchedId?.rObject.orEmpty(),
                            reserchedId?.cIdentSystem.orEmpty()
                        )
                    } else {
                        Completable.fromAction { }
                    }
                ).subscribe({
                    viewState.showIdsUpdatedSuccess()
                }, {
                    it.printStackTrace()
                })
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
    }


}
