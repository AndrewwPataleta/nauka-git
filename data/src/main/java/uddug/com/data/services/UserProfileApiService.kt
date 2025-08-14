package uddug.com.data.services

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import okhttp3.MultipartBody
import uddug.com.data.services.models.request.user_profile.NickNameCheckRequestDto
import uddug.com.data.services.models.request.user_profile.UserProfileRequestDto
import uddug.com.data.services.models.response.user_profile.CheckNickNameResponseDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import uddug.com.data.services.models.request.user_profile.AddUserAcademicDegreesDto
import uddug.com.data.services.models.request.user_profile.NickNameChangeRequestDto
import uddug.com.data.services.models.request.user_profile.UpdateUserAuthorInfoDto
import uddug.com.data.services.models.request.user_profile.UpdateUserProfileEducationDto
import uddug.com.data.services.models.request.user_profile.UpdateUserProfileLaborDto
import uddug.com.data.services.models.request.user_profile.UserProfileShortRequestDto
import uddug.com.data.services.models.response.user_profile.UserProfileFullInfoDto
import uddug.com.domain.entities.profile.Addresses
import uddug.com.domain.entities.profile.ContactData
import uddug.com.domain.entities.feed.FeedContainer
import uddug.com.domain.entities.feed.PostComment
import uddug.com.domain.entities.feed.PostCommentAddRequest
import uddug.com.domain.entities.feed.PostCommentRequest
import uddug.com.domain.entities.profile.NewPasswordDataRequest
import uddug.com.domain.entities.profile.SettingsForm
import uddug.com.domain.entities.profile.WritableItem
import uddug.com.domain.interactors.user_profile.model.DefaultCls

interface UserProfileApiService {

    @POST("core/user_profile/info")
    fun setUser(
        //@Header("Cookie") _nkts: String? = null,
        //@Header(" _nkthnt") _nkthnt: String? = null,
        @Body userProfileRequestDto: UserProfileRequestDto
    ): Single<String>

    @GET("core/user_profile")
    fun getUserInfo(): Single<UserProfileFullInfoDto>

    @POST("core/user_profile/info/check-nickname-free")
    fun checkNickName(
        @Body nickName: NickNameCheckRequestDto
    ): Observable<CheckNickNameResponseDto>

    @POST("core/user_profile/info")
    fun updateNickname(
        @Body nickName: NickNameChangeRequestDto
    ): Completable

    @POST("core/user_profile/info/check-nickname-free")
    fun updateUserId(
        @Body nickName: NickNameCheckRequestDto
    ): Observable<CheckNickNameResponseDto>

    @DELETE("core/user_profile/info/banner/{id}")
    fun deleteUserBanner(
        @Path("id") userId: String,
    ): Completable

    @DELETE("core/user_profile/info/image/{id}")
    fun deleteUserPhoto(
        @Path("id") userId: String,
    ): Completable

    @Multipart
    @PUT("core/user_profile/info/image/{id}")
    fun uploadUserPhoto(
        @Path("id") userId: String,
        @Part filePart: MultipartBody.Part
    ): Completable

    @Multipart
    @PUT("core/user_profile/info/banner/{id}")
    fun uploadUserBanner(
        @Path("id") userId: String,
        @Part filePart: MultipartBody.Part
    ): Completable

    @POST("core/user_profile/info")
    fun updateShortUser(
        @Body userProfileRequestDto: UserProfileShortRequestDto
    ): Completable

    @DELETE("core/user_profile/{user_id}/education/{education_id}")
    fun removeEducationInfo(
        @Path("user_id") userId: String,
        @Path("education_id") educationId: String,
    ): Completable

    @DELETE("core/user_profile/{user_id}/labor_activity/{labor_activity_id}")
    fun removeLaborActivityInfo(
        @Path("user_id") userId: String,
        @Path("labor_activity_id") laborActivityId: String,
    ): Completable

    @PATCH("core/user_profile/{user_id}/education/{education_id}")
    fun updateEducationInfo(
        @Path("user_id") userId: String,
        @Path("education_id") educationId: String,
        @Body userEducation: UpdateUserProfileEducationDto
    ): Completable


    @PATCH("core/user_profile/{user_id}/labor_activity/{labor_activity_id}")
    fun updateLaborInfo(
        @Path("user_id") userId: String,
        @Path("labor_activity_id") educationId: String,
        @Body userEducation: UpdateUserProfileLaborDto
    ): Completable


    @POST("core/user_profile/{user_id}/education")
    fun createEducationInfo(
        @Path("user_id") userId: String,
        @Body userEducation: List<UpdateUserProfileEducationDto>
    ): Completable

    @POST("core/user_profile/{user_id}/labor_activity")
    fun createLaborActivityInfo(
        @Path("user_id") userId: String,
        @Body userEducation: List<UpdateUserProfileLaborDto>
    ): Completable

    @PATCH("core/obj_identifier/{obj_id}")
    fun updateUserAuthorInfo(
        @Path("obj_id") objId: String,
        @Body authorInfo: UpdateUserAuthorInfoDto
    ): Completable

    @PATCH("core/obj_identifier/{obj_id}")
    fun updateUserObjId(
        @Path("obj_id") objId: String,
        @Body authorInfo: UpdateUserAuthorInfoDto
    ): Completable

    @GET("a/oauth2c/validate")
    fun validateUser(
    ): Completable

    @POST("core/user_profile/{user_id}/user_academic_degrees/")
    fun addUserAcademicDegree(
        @Path("user_id") userId: String,
        @Body degrees: List<AddUserAcademicDegreesDto>
    ): Completable

    @PATCH("core/user_profile/{user_id}/user_academic_degrees/")
    fun updateUserAcademicDegree(
        @Path("user_id") userId: String,
        @Body degrees: List<AddUserAcademicDegreesDto>
    ): Completable

    @PATCH("core/address/{address_id}")
    fun updateUserAddress(
        @Path("address_id") addressId: String,
        @Body address: Addresses
    ): Completable

    @POST("core/user_profile/{user_id}/contact")
    fun saveUserContacts(
        @Path("user_id") userId: String,
        @Body contacts: List<ContactData>
    ): Completable

    @POST("a/oauth2c/change-password")
    fun updateNewPassword(
        @Body newPassword: NewPasswordDataRequest
    ): Completable

    @PATCH("core/user_profile/{user_id}/contact")
    fun updateUserContacts(
        @Path("user_id") userId: String,
        @Body contacts: List<ContactData>
    ): Completable

    @GET("core/settings/model")
    fun getUserSettings(): Single<SettingsForm>

    @GET("core/cls/all")
    fun getCls(
        @Query("cls") cls: Int,
        @Query("pageSize") pageSize: Int = 1000
    ): Single<List<DefaultCls>>

    @PATCH("core/user_profile/settings")
    fun updateUserSettings(
        @Body settings: Map<String, String>
    ): Completable

    @POST("core/feed/items/by_owner/{user_id}")
    fun getUserFeeds(
        @Path("user_id") userId: String,
    ): Single<List<FeedContainer>>

    @POST("core/user_messages/list/{user_id}")
    fun getUsersMessagesPosts(
        @Path("user_id") userId: String,
    ): Single<List<FeedContainer>>

    @GET("core/feed/writable")
    fun getWritable(
    ): Single<List<WritableItem>>

    @POST("core/bookmark")
    fun addPostToFavorite(
        @Body feedContainer: FeedContainer
    ): Completable

    @POST("core/post/comment/fetch")
    fun getPostComments(
        @Body request: PostCommentRequest
    ): Single<List<PostComment>>

    @POST("core/post/comment")
    fun sendPostComment(
        @Body postCommentAddRequest: PostCommentAddRequest
    ): Single<PostComment>
}
