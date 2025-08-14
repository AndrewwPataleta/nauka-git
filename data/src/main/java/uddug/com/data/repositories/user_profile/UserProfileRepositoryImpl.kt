package uddug.com.data.repositories.user_profile

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import toothpick.InjectConstructor
import uddug.com.data.cache.user_uuid.UserUUIDCache
import uddug.com.data.services.UserProfileApiService
import uddug.com.data.services.models.request.user_profile.AddUserAcademicDegreesDto
import uddug.com.data.services.models.request.user_profile.NickNameChangeRequestDto
import uddug.com.data.services.models.request.user_profile.NickNameCheckRequestDto
import uddug.com.data.services.models.request.user_profile.UpdateUserAuthorInfoDto
import uddug.com.data.services.models.request.user_profile.UpdateUserProfileEducationDto
import uddug.com.data.services.models.request.user_profile.UpdateUserProfileLaborDto
import uddug.com.data.services.models.request.user_profile.UserProfileShortRequestDto
import uddug.com.data.utils.toDomain
import uddug.com.domain.entities.profile.Addresses
import uddug.com.domain.entities.profile.ContactData
import uddug.com.domain.entities.profile.Education
import uddug.com.domain.entities.feed.FeedContainer
import uddug.com.domain.entities.feed.PostComment
import uddug.com.domain.entities.feed.PostCommentAddRequest
import uddug.com.domain.entities.feed.PostCommentRequest
import uddug.com.domain.entities.profile.LaborActivities
import uddug.com.domain.entities.profile.NewPasswordDataRequest
import uddug.com.domain.entities.profile.SettingsForm
import uddug.com.domain.entities.profile.UserProfileFullInfo
import uddug.com.domain.entities.profile.WritableItem
import uddug.com.domain.interactors.user_profile.model.DefaultCls
import uddug.com.domain.interactors.user_profile.model.ShortInfoUi
import uddug.com.domain.interactors.user_profile.model.ShortInfoUpdate
import uddug.com.domain.repositories.models.UserAcademicDegrees
import uddug.com.domain.repositories.user_profile.UserProfileRepository
import java.io.File
import java.util.Calendar
import java.util.GregorianCalendar


@InjectConstructor
class UserProfileRepositoryImpl(
    private val profileApiService: UserProfileApiService,
    private val userProfileMapper: UserProfileMapper,
    private val userUUIDCache: UserUUIDCache
) : UserProfileRepository {

    override fun getProfileInfo(): Single<UserProfileFullInfo> {
        return profileApiService.getUserInfo().map { it.toDomain() }
    }

    override fun updateNickname(
        id: String,
        nickname: String,
        firstName: String,
        lastName: String
    ): Completable {
        return profileApiService.updateNickname(
            NickNameChangeRequestDto(
                id = id,
                nickname = nickname,
                firstName = firstName,
                lastName = lastName
            )
        )
    }

    override fun deleteUserAvatar(userId: String): Completable {
        return profileApiService.deleteUserPhoto(
            userId = userId
        )
    }

    override fun deleteUserBanner(userId: String): Completable {
        return profileApiService.deleteUserBanner(
            userId = userId
        )
    }

    override fun uploadUserAvatar(userId: String, file: File): Completable {
        return profileApiService.uploadUserPhoto(
            userId = userId,
            filePart = MultipartBody.Part.createFormData(
                "image",
                file.getName(),
                RequestBody.create("image/*".toMediaTypeOrNull(), file)
            )
        )
    }

    override fun uploadUserBanner(userId: String, file: File): Completable {
        return profileApiService.uploadUserBanner(
            userId = userId,
            filePart = MultipartBody.Part.createFormData(
                "image",
                file.getName(),
                RequestBody.create("image/*".toMediaTypeOrNull(), file)
            )
        )
    }

    override fun updateShortInfoProfile(shortInfoUpdate: ShortInfoUpdate): Completable {
        return profileApiService.updateShortUser(
            UserProfileShortRequestDto(
                id = shortInfoUpdate.id,
                firstName = shortInfoUpdate.name,
                lastName = shortInfoUpdate.surname,
                middleName = shortInfoUpdate.middleName,
                birthDate = shortInfoUpdate.birthday,
                gender = shortInfoUpdate.gender,
                dsc = shortInfoUpdate.description,
                nickname = shortInfoUpdate.nickname,
            )
        )
    }

    override fun removeUserEducation(userId: String, education: Education): Completable {
        return profileApiService.removeEducationInfo(
            userId = userId,
            educationId = education.id.toString()
        )
    }

    override fun removeUserLaborActivity(userId: String, labor: LaborActivities): Completable {
        return profileApiService.removeLaborActivityInfo(
            userId = userId,
            laborActivityId = labor.id.toString()
        )
    }

    override fun updateUserEducation(userId: String, education: Education): Completable {

        val calendar: GregorianCalendar = GregorianCalendar()

        var startDate: String? = null
        education.startDate?.toIntOrNull()?.let {
            calendar.set(it, Calendar.JULY, 31)
            startDate = calendar.toZonedDateTime().toLocalDate().toString()
        }

        var endDate: String? = null
        education.endDate?.toIntOrNull()?.let {
            calendar.set(it, Calendar.JULY, 31)
            endDate = calendar.toZonedDateTime().toLocalDate().toString()
        }
        val userEducation = UpdateUserProfileEducationDto(
            cCountry = education.country?.uref,
            name = education.name,
            city = education.city
        ).apply {
            if (endDate != null) {
                this.endDate = endDate
            }
            if (startDate != null) {
                this.startDate = startDate
            }
        }

        return profileApiService.updateEducationInfo(
            userId = userId,
            educationId = education.id.toString(),
            userEducation = userEducation
        )
    }

    override fun updateUserLaborActivities(userId: String, labor: LaborActivities): Completable {
        val calendar: GregorianCalendar = GregorianCalendar()

        var startDate: String? = null
        labor.startWork?.toIntOrNull()?.let {
            calendar.set(it, Calendar.JULY, 31)
            startDate = calendar.toZonedDateTime().toLocalDate().toString()
        }

        var endDate: String? = null
        labor.endWork?.toIntOrNull()?.let {
            calendar.set(it, Calendar.JULY, 31)
            endDate = calendar.toZonedDateTime().toLocalDate().toString()
        }
        val userEducation = UpdateUserProfileLaborDto(
            cCountry = labor.country?.uref,
            position = labor.position,
            city = labor.city
        ).apply {
            if (endDate != null) {
                this.endWork = endDate
            }
            if (startDate != null) {
                this.startWork = startDate
            }
        }

        return profileApiService.updateLaborInfo(
            userId = userId,
            educationId = labor.id.toString(),
            userEducation = userEducation
        )
    }

    override fun createUserEducation(userId: String, education: List<Education>): Completable {

        val education = education.first()

        val userEducation = UpdateUserProfileEducationDto(
            cCountry = education.country?.uref,
            name = education.name,
            city = education.city,
            cLevel = education.cLevel,
            startDate = education.startDate,
            endDate = education.endDate
        )
        return profileApiService.createEducationInfo(
            userId = userId,
            userEducation = listOf(userEducation)
        )
    }

    override fun createUserLabor(userId: String, labor: LaborActivities): Completable {

        val userEducation = UpdateUserProfileLaborDto(
            cCountry = labor.country?.uref,
            position = labor.position,
            city = labor.city,
            orgName = labor.orgName,
            startWork = labor.startWork,
            endWork = labor.endWork
        )
        return profileApiService.createLaborActivityInfo(
            userId = userId,
            userEducation = listOf(userEducation)
        )
    }

    override fun addUserAcademicDegrees(
        userId: String,
        degrees: List<UserAcademicDegrees>
    ): Completable {
        return profileApiService.addUserAcademicDegree(
            userId = userId,
            degrees = degrees.map {
                AddUserAcademicDegreesDto(
                    name = it.name,
                    titleDate = it.titleDate
                )
            }
        )
    }

    override fun updateUserAcademicDegrees(
        userId: String,
        degrees: List<UserAcademicDegrees>
    ): Completable {
        return profileApiService.updateUserAcademicDegree(
            userId = userId,
            degrees = degrees.map {
                AddUserAcademicDegreesDto(
                    name = it.name,
                    titleDate = it.titleDate
                )
            }
        )
    }

    override fun updateUserObjectId(
        userId: String,
        objectId: String,
        updatedId: String,
        rObject: String,
        cIdentSystem: String
    ): Completable {
        return profileApiService.updateUserObjId(
            objId = objectId,
            authorInfo = UpdateUserAuthorInfoDto(
                identifier = updatedId,
                rObject = rObject,
                cIdentSystem = cIdentSystem
            )
        )
    }

    override fun updateUserAddress(address: Addresses): Completable {
        return profileApiService.updateUserAddress(
            addressId = address.id.orEmpty(),
            address = address
        )
    }

    override fun updateUserContacts(userId: String, contacts: List<ContactData>): Completable {
        return profileApiService.updateUserContacts(
            userId = userId,
            contacts = contacts
        )
    }

    override fun saveUserContacts(userId: String, contacts: List<ContactData>): Completable {
        return profileApiService.saveUserContacts(
            userId = userId,
            contacts = contacts
        )
    }

    override fun updateNewPassword(newPassword: String, currentPassword: String): Completable {
        return profileApiService.updateNewPassword(
            newPassword = NewPasswordDataRequest(
                confirmation = newPassword,
                newPassword = newPassword,
                currentPassword = currentPassword
            )
        )
    }

    override fun getUserSettings(): Single<SettingsForm> {
        return profileApiService.getUserSettings(

        )
    }

    override fun getUserCls(pageSize: Int, cls: Int): Single<List<DefaultCls>> {
        return profileApiService.getCls(
            cls = cls, pageSize = pageSize
        )
    }

    override fun updateUserSettings(settings: MutableMap<String, String>): Completable {
        return profileApiService.updateUserSettings(
            settings
        )
    }

    override fun getFeedWritable(): Single<List<WritableItem>> {
        return profileApiService.getWritable()
    }

    override fun getUserFeed(userId: String): Single<List<FeedContainer>> {
        return profileApiService.getUserFeeds(
            userId
        )
    }

    override fun getUserFeedsRecommendations(userId: String): Single<List<FeedContainer>> {
        return profileApiService.getUsersMessagesPosts(
            userId
        )
    }

    override fun addToFavorite(feedContainer: FeedContainer): Completable {
        return profileApiService.addPostToFavorite(
            feedContainer = feedContainer
        )
    }

    override fun hideAuthorPosts(feedContainer: FeedContainer): Completable {
        return Completable.fromAction { }
    }

    override fun getPostComments(postId: String): Single<List<PostComment>> {
        return profileApiService.getPostComments(
            PostCommentRequest(
                asc = false,
                pageSize = 10,
                postId = postId,
                view = "tree"
            )
        )
    }

    override fun sendComment(postComment: PostCommentAddRequest): Single<PostComment> {
        return profileApiService.sendPostComment(
            postComment
        )
    }

    override fun setUser(shortInfoEntity: ShortInfoUi): Completable {
        return profileApiService.setUser(
            userProfileRequestDto = userProfileMapper.mapDomainToDto(shortInfoEntity)
        )
            .flatMapCompletable {
                userUUIDCache.entity = it
                Completable.complete()
            }
    }

    override fun validateProfile(): Completable {
        return profileApiService.validateUser()
    }

    override fun checkNickname(nickname: String): Observable<Boolean> {
        return profileApiService
            .checkNickName(NickNameCheckRequestDto(nickname = "id$nickname"))
            .flatMap { Observable.just(it.nicknameIsFree) }
    }


}
