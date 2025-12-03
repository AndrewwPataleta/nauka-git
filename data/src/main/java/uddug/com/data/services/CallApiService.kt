package uddug.com.data.services

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import uddug.com.data.services.models.request.call.UpdateCallPermitsRequestDto
import uddug.com.data.services.models.request.call.UpdateCallStateRequestDto
import uddug.com.data.services.models.request.call.UpdateCallStatusRequestDto
import uddug.com.data.services.models.response.call.CallParticipantDto

interface CallApiService {

    @GET("calls/dialog/{dialogId}/participants")
    suspend fun getParticipants(
        @Path("dialogId") dialogId: Long,
    ): List<CallParticipantDto>

    @PATCH("calls/dialog/{dialogId}/status")
    suspend fun updateStatus(
        @Path("dialogId") dialogId: Long,
        @Body request: UpdateCallStatusRequestDto,
    )

    @PATCH("calls/dialog/{dialogId}/permits")
    suspend fun updatePermits(
        @Path("dialogId") dialogId: Long,
        @Body request: UpdateCallPermitsRequestDto,
    )

    @PATCH("calls/dialog/{dialogId}/state")
    suspend fun updateState(
        @Path("dialogId") dialogId: Long,
        @Body request: UpdateCallStateRequestDto,
    )

    @POST("calls/{callId}/stop")
    suspend fun stopCall(
        @Path("callId") callId: Long,
    )
}
