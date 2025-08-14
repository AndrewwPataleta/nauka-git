package uddug.com.domain.interactors.user_profile

import androidx.paging.Pager
import androidx.paging.PagingConfig
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import uddug.com.domain.SchedulersProvider
import uddug.com.domain.entities.HttpException
import uddug.com.domain.entities.ServerApiError
import uddug.com.domain.interactors.user_profile.model.ShortInfoUi
import uddug.com.domain.repositories.user_profile.UserProfileRepository
import toothpick.InjectConstructor
import uddug.com.domain.entities.profile.Addresses
import uddug.com.domain.entities.profile.ContactData
import uddug.com.domain.entities.profile.Education
import uddug.com.domain.entities.feed.FeedContainer
import uddug.com.domain.entities.feed.PostComment
import uddug.com.domain.entities.feed.PostCommentAddRequest
import uddug.com.domain.entities.profile.LaborActivities
import uddug.com.domain.entities.profile.SettingsForm
import uddug.com.domain.entities.profile.UserProfileFullInfo
import uddug.com.domain.entities.profile.WritableItem
import uddug.com.domain.interactors.user_profile.model.DefaultCls
import uddug.com.domain.interactors.user_profile.model.ShortInfoUpdate
import uddug.com.domain.repositories.models.UserAcademicDegrees
import java.io.File

@InjectConstructor
class UserProfileInteractor(
    private val userProfileRepository: UserProfileRepository,
    private val schedulers: SchedulersProvider,
) {

    fun getUserProfilePreviewInfo(): Single<UserProfileFullInfo> {
        return userProfileRepository.getProfileInfo()
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
    }

    fun validateUser(): Completable {
        return userProfileRepository.validateProfile()
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
    }

    fun setUser(shortInfoEntity: ShortInfoUi): Completable {
        return userProfileRepository.setUser(shortInfoEntity)
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
    }

    fun checkNickname(nickname: String): Observable<Boolean> {
        return userProfileRepository.checkNickname(nickname)
            //.onErrorReturnItem(false)
            .onErrorReturn {
                if (it is HttpException && it.statusCode == ServerApiError.Unauthorized) {
                    throw it
                }
                false
            }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
    }


    fun updateUserId(
        id: String,
        nickname: String,
        firstname: String,
        lastname: String,
    ): Completable {
        return userProfileRepository.updateNickname(
            id = id,
            nickname = nickname,
            firstname = firstname,
            lastname = lastname
        )
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
    }

    fun deleteUserAvatar(userId: String): Completable {
        return userProfileRepository.deleteUserAvatar(userId = userId)
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
    }

    fun deleteUserBanner(userId: String): Completable {
        return userProfileRepository.deleteUserBanner(userId = userId)
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
    }


    fun uploadUserAvatar(userId: String, avatar: File): Completable {
        return userProfileRepository.uploadUserAvatar(userId = userId, file = avatar)
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
    }

    fun uploadUserBanner(userId: String, avatar: File): Completable {
        return userProfileRepository.uploadUserBanner(userId = userId, file = avatar)
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
    }

    fun updateProfileShortInfo(shortInfoUpdate: ShortInfoUpdate): Completable {
        return userProfileRepository.updateShortInfoProfile(shortInfoUpdate)
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
    }


    fun removeUserEducation(userId: String, education: Education): Completable {
        return userProfileRepository.removeUserEducation(userId, education)
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
    }

    fun removeUserLaborActivity(userId: String, laborActivities: LaborActivities): Completable {
        return userProfileRepository.removeUserLaborActivity(userId, laborActivities)
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
    }

    fun updateUserEducation(userId: String, education: Education): Completable {
        return userProfileRepository.updateUserEducation(userId, education)
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
    }

    fun updateUserCarrier(userId: String, labor: LaborActivities): Completable {
        return userProfileRepository.updateUserLaborActivities(userId, labor)
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
    }

    fun createUserEducation(userId: String, education: List<Education>): Completable {
        return userProfileRepository.createUserEducation(userId, education)
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
    }

    fun createUserLabor(userId: String, labor: LaborActivities): Completable {
        return userProfileRepository.createUserLabor(userId, labor)
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
    }

    fun addUserAcademic(userId: String, degrees: List<UserAcademicDegrees>): Completable {
        return userProfileRepository.addUserAcademicDegrees(userId, degrees)
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
    }

    fun updateUserAcademic(userId: String, degrees: List<UserAcademicDegrees>): Completable {
        return userProfileRepository.updateUserAcademicDegrees(userId, degrees)
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
    }

    fun updateUserObjectId(
        userId: String,
        objectId: String,
        updatedId: String,
        rObject: String,
        cIdentSystem: String,
    ): Completable {
        return userProfileRepository.updateUserObjectId(
            userId,
            objectId,
            updatedId,
            rObject,
            cIdentSystem
        )
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
    }

    fun updateAddress(
        address: Addresses,
    ): Completable {
        return userProfileRepository.updateUserAddress(
            address = address
        )
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
    }

    fun updateContacts(
        userId: String,
        contacts: List<ContactData>,
    ): Completable {
        return userProfileRepository.updateUserContacts(
            userId,
            contacts = contacts
        )
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
    }

    fun saveContacts(
        userId: String,
        contacts: List<ContactData>,
    ): Completable {
        return userProfileRepository.saveUserContacts(
            userId,
            contacts = contacts
        )
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
    }

    fun updatePassword(
        newPassword: String,
        currentPassword: String,
    ): Completable {
        return userProfileRepository.updateNewPassword(
            newPassword = newPassword,
            currentPassword = currentPassword
        )
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
    }

    fun getUserSettings(): Single<SettingsForm> {
        return userProfileRepository.getUserSettings()
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
    }


    fun getUserCls(
        pageSize: Int = 1000,
        cls: Int,
    ): Single<List<DefaultCls>> {
        return userProfileRepository.getUserCls(
            pageSize = pageSize,
            cls = cls
        )
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
    }

    fun updateCurrentSettings(
        settings: MutableMap<String, String>,
    ): Completable {
        return userProfileRepository.updateUserSettings(
            settings = settings
        )
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
    }

    fun getFeedWritable(
    ): Single<List<WritableItem>> {
        return userProfileRepository.getFeedWritable(
        )
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
    }

    fun getUserFeed(
        userId: String,
    ): Single<List<FeedContainer>> {
        return userProfileRepository.getUserFeed(
            userId = userId,
        )
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
    }

    fun getUserFeedsRecommends(
        userId: String,
    ): Single<List<FeedContainer>> {
        return userProfileRepository.getUserFeedsRecommendations(
            userId = userId,
        )
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
    }

    fun addToFavorite(
        feedContainer: FeedContainer,
    ): Completable {
        return userProfileRepository.addToFavorite(
            feedContainer
        )
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
    }

    fun hideAuthorPosts(
        feedContainer: FeedContainer,
    ): Completable {
        return userProfileRepository.hideAuthorPosts(
            feedContainer
        )
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
    }

    fun getWallComments(
        postId: String,
    ): Single<List<PostComment>> {
        return userProfileRepository.getPostComments(
            postId
        )
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
    }

    fun sendComment(
        postComment: PostCommentAddRequest,
    ): Single<PostComment> {
        return userProfileRepository.sendComment(
            postComment
        )
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
    }

    companion object {
        const val FIST_PAGE = 1
        const val DEFAULT_PAGE_SIZE = 1
        const val INITIAL_PAGE_SIZE = DEFAULT_PAGE_SIZE * 2
        const val WORD_COUNT_IN_SITUATION = 3
    }
}
