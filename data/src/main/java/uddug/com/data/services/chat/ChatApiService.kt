package uddug.com.data.services.chat

import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import uddug.com.data.services.models.request.chat.CreateDialogRequestDto
import uddug.com.data.services.models.request.chat.DeleteMessagesRequestDto
import uddug.com.data.services.models.request.chat.PinMessageRequestDto
import uddug.com.data.services.models.request.chat.ReadMessagesRequestDto
import uddug.com.data.services.models.request.chat.UpdateMessageRequestDto
import uddug.com.data.services.models.response.chat.ChatDto
import uddug.com.data.services.models.response.chat.DialogInfoDto
import uddug.com.data.services.models.response.chat.FoldersDto
import uddug.com.data.services.models.response.chat.MessageDto
import uddug.com.data.services.models.response.chat.FileDto
import uddug.com.data.services.models.response.user_profile.UserProfileFullInfoDto
import uddug.com.domain.entities.chat.MediaMessage

interface ChatApiService {

    @GET("chat/v1/dialogs")
    suspend fun getDialogs(@Query("folderId") folderId: Long? = null): ChatDto

    @GET("chat/v1/dialogs/folder")
    suspend fun getFolders(): FoldersDto

    @GET("chat/v1/dialogs/{dialogId}")
    suspend fun getMessages(
        @Path("dialogId") dialogId: Long,
        @Query("limit") limit: Int,
        @Query("lastMessageId") lastMessageId: Long? = null,
    ): List<MessageDto>

    @GET("chat/v1/dialogs/info/{dialogId}")
    suspend fun getDialogInfo(
        @Path("dialogId") dialogId: Long,
    ): DialogInfoDto

    @GET("chat/v1/dialogs/media/{dialogId}")
    suspend fun getDialogMedia(
        @Path("dialogId") dialogId: Long,
        @Query("category") category: Int,
        @Query("limit") limit: Int? = null,
        @Query("page") page: Int? = null,
        @Query("q") query: String? = null,
        @Query("sd") sd: String? = null,
        @Query("ed") ed: String? = null,
    ): List<MediaMessage>

    @POST("chat/v1/dialogs/{userId}")
    suspend fun createDialog(
        @Path("userId") userId: Long,
    ): DialogInfoDto

    @POST("chat/v2/dialogs/create")
    suspend fun createGroupDialog(
        @Body request: CreateDialogRequestDto,
    ): DialogInfoDto

    @POST("chat/v1/dialogs/pin/{dialogId}")
    suspend fun pinDialog(@Path("dialogId") dialogId: Long)

    @POST("chat/v1/dialogs/unpin/{dialogId}")
    suspend fun unpinDialog(@Path("dialogId") dialogId: Long)

    @PATCH("chat/v1/messages/update")
    suspend fun updateMessage(@Body request: UpdateMessageRequestDto): MessageDto

    @PATCH("chat/v1/messages/read")
    suspend fun markMessagesRead(@Body request: ReadMessagesRequestDto)

    @PATCH("chat/v1/messages/pin")
    suspend fun pinMessage(@Body request: PinMessageRequestDto)

    @PATCH("chat/v1/messages/unpin")
    suspend fun unpinMessage(@Body request: PinMessageRequestDto)

    @DELETE("chat/v1/messages/{messageId}")
    suspend fun deleteMessage(
        @Path("messageId") messageId: Long,
        @Query("forMe") forMe: Boolean = false,
    )

    @HTTP(method = "DELETE", path = "chat/v1/messages", hasBody = true)
    suspend fun deleteMessages(
        @Body request: DeleteMessagesRequestDto,
        @Query("forMe") forMe: Boolean = false,
    )

    @GET("chat/v1/users/search")
    suspend fun searchUsers(
        @Query("searchField") searchField: String,
        @Query("limit") limit: Int = 10,
        @Query("page") page: Int = 1,
    ): List<UserProfileFullInfoDto>

    @Multipart
    @POST("core/files")
    suspend fun uploadFiles(
        @Part files: List<MultipartBody.Part>,
        @Query("raw") raw: Boolean = false,
    ): List<FileDto>
}
