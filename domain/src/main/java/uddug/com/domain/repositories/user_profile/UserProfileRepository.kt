package uddug.com.domain.repositories.user_profile

import androidx.paging.PagingSource
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
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
import uddug.com.domain.interactors.user_profile.model.ShortInfoUi
import uddug.com.domain.interactors.user_profile.model.ShortInfoUpdate
import uddug.com.domain.repositories.models.UserAcademicDegrees
import java.io.File

interface UserProfileRepository {
    fun setUser(shortInfoEntity: ShortInfoUi): Completable
    fun validateProfile(): Completable
    fun checkNickname(nickname: String): Observable<Boolean>
    fun getProfileInfo(): Single<UserProfileFullInfo>
    fun updateNickname(
        id: String,
        nickname: String,
        firstname: String,
        lastname: String
    ): Completable

    fun deleteUserAvatar(userId: String): Completable
    fun deleteUserBanner(userId: String): Completable
    fun uploadUserAvatar(userId: String, file: File): Completable
    fun uploadUserBanner(userId: String, file: File): Completable
    fun updateShortInfoProfile(shortInfoUpdate: ShortInfoUpdate): Completable
    fun removeUserEducation(userId: String, education: Education): Completable
    fun removeUserLaborActivity(userId: String, labor: LaborActivities): Completable
    fun updateUserEducation(userId: String, education: Education): Completable
    fun updateUserLaborActivities(userId: String, labor: LaborActivities): Completable
    fun createUserEducation(userId: String, education: List<Education>): Completable
    fun createUserLabor(userId: String, labor: LaborActivities): Completable
    fun addUserAcademicDegrees(userId: String, degrees: List<UserAcademicDegrees>): Completable
    fun updateUserAcademicDegrees(userId: String, degrees: List<UserAcademicDegrees>): Completable
    fun updateUserObjectId(
        userId: String, objectId: String, updatedId: String, rObject: String,
        cIdentSystem: String
    ): Completable

    fun updateUserAddress(
        address: Addresses
    ): Completable

    fun updateUserContacts(
        userId: String,
        contacts: List<ContactData>
    ): Completable

    fun saveUserContacts(
        userId: String,
        contacts: List<ContactData>
    ): Completable

    fun updateNewPassword(
        newPassword: String,
        currentPassword: String
    ): Completable

    fun getUserSettings(): Single<SettingsForm>

    fun getUserCls(
        pageSize: Int = 1000,
        cls: Int
    ): Single<List<DefaultCls>>

    fun updateUserSettings(
        settings: MutableMap<String, String>
    ): Completable

    fun getFeedWritable(
    ): Single<List<WritableItem>>

    fun getUserFeed(
        userId: String,
    ): Single<List<FeedContainer>>

    fun getUserFeedsRecommendations(
        userId: String,
    ): Single<List<FeedContainer>>

    fun addToFavorite(
        feedContainer: FeedContainer
    ): Completable

    fun hideAuthorPosts(
        feedContainer: FeedContainer
    ): Completable

    fun getPostComments(
        postId: String
    ): Single<List<PostComment>>

    fun sendComment(
        postComment: PostCommentAddRequest
    ): Single<PostComment>

}
