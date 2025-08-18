package uddug.com.data.services.chat

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import uddug.com.data.services.models.request.chat.DeleteMessagesRequestDto
import uddug.com.data.services.models.request.chat.ReadMessagesRequestDto
import uddug.com.data.services.models.request.chat.UpdateMessageRequestDto
import uddug.com.data.services.models.response.chat.ChatDto
import uddug.com.data.services.models.response.chat.DialogInfoDto
import uddug.com.data.services.models.response.chat.MessageDto
import uddug.com.domain.entities.chat.MediaMessage

interface ChatApiService {

    @GET("chat/v1/dialogs")
    suspend fun getDialogs(): ChatDto

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

    @PATCH("chat/v1/messages/update")
    suspend fun updateMessage(@Body request: UpdateMessageRequestDto): MessageDto

    @PATCH("chat/v1/messages/read")
    suspend fun markMessagesRead(@Body request: ReadMessagesRequestDto)

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
}
