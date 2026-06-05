package uddug.com.data.services

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import uddug.com.data.services.models.request.call.StartRecordingRequestDto
import uddug.com.data.services.models.request.call.UpdateCallPermitsRequestDto
import uddug.com.data.services.models.request.call.UpdateCallStateRequestDto
import uddug.com.data.services.models.request.call.UpdateCallStatusRequestDto
import uddug.com.data.services.models.response.call.CallParticipantDto

interface CallApiService {

    @GET("chat/v1/calls/dialog/{dialogId}/participants")
    suspend fun getParticipants(
        @Path("dialogId") dialogId: Long,
        // details bitmask: 1 — include fullName & imageUrl. See docs/calls.md.
        @Query("details") details: Int = 1,
    ): List<CallParticipantDto>

    @PATCH("chat/v1/calls/dialog/{dialogId}/status")
    suspend fun updateStatus(
        @Path("dialogId") dialogId: Long,
        @Body request: UpdateCallStatusRequestDto,
    )

    @PATCH("chat/v1/calls/dialog/{dialogId}/permits")
    suspend fun updatePermits(
        @Path("dialogId") dialogId: Long,
        @Body request: UpdateCallPermitsRequestDto,
    )

    @PATCH("chat/v1/calls/dialog/{dialogId}/state")
    suspend fun updateState(
        @Path("dialogId") dialogId: Long,
        @Body request: UpdateCallStateRequestDto,
    )

    @POST("chat/v1/calls/{callId}/stop")
    suspend fun stopCall(
        @Path("callId") callId: Long,
    )

    @PUT("chat/v1/calls/record/start/dialog/{dialogId}")
    suspend fun startRecording(
        @Path("dialogId") dialogId: Long,
        @Body request: StartRecordingRequestDto,
    )

    @PUT("chat/v1/calls/record/stop/dialog/{dialogId}")
    suspend fun stopRecording(
        @Path("dialogId") dialogId: Long,
    )
}
