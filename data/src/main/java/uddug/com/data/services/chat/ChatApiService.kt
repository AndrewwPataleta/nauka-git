package uddug.com.data.services.chat

import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import uddug.com.data.services.models.response.chat.ChatDto
import uddug.com.data.services.models.response.chat.DialogInfoDto
import uddug.com.data.services.models.response.chat.MessageDto
import uddug.com.domain.entities.chat.DialogInfo
import uddug.com.domain.entities.chat.MediaMessage
import uddug.com.domain.entities.chat.MessageChat

interface ChatApiService {

    @GET("chat/v1/dialogs")
    suspend fun getDialogs(): ChatDto

    @GET("chat/v1/dialogs/{dialogId}")
    suspend fun getMessages(
        @Path("dialogId") dialogId: Long,
        @Query("limit") limit: Int,
    ): List<MessageDto>

    @GET("chat/v1/dialogs/info/{dialogId}")
    suspend fun getDialogInfo(
        @Path("dialogId") dialogId: Long
    ): DialogInfoDto

    @GET("chat/v1/dialogs/media/{dialogId}")
    suspend fun getDialogMedia(
        @Path("dialogId") dialogId: Long,
        @Query("category") category: Int,
    ): List<MediaMessage>

    @POST("chat/v1/dialogs/{userId}")
    suspend fun createDialog(
        @Path("userId") userId: Long,
    ): DialogInfoDto
}
