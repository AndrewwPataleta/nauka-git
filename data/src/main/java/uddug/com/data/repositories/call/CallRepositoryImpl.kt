package uddug.com.data.repositories.call

import javax.inject.Inject
import uddug.com.data.mapper.toDomain
import uddug.com.data.mapper.toDto
import uddug.com.data.services.CallApiService
import uddug.com.data.services.models.request.call.UpdateCallPermitsRequestDto
import uddug.com.data.services.models.request.call.UpdateCallStateRequestDto
import uddug.com.data.services.models.request.call.UpdateCallStatusRequestDto
import uddug.com.domain.entities.call.CallParticipant
import uddug.com.domain.entities.call.CallSessionState
import uddug.com.domain.repositories.call.CallRepository

class CallRepositoryImpl @Inject constructor(
    private val callApiService: CallApiService,
) : CallRepository {

    override suspend fun getParticipants(dialogId: Long): List<CallParticipant> {
        return callApiService.getParticipants(dialogId).map { it.toDomain() }
    }

    override suspend fun updateStatus(dialogId: Long, userId: String, status: Int) {
        callApiService.updateStatus(
            dialogId = dialogId,
            request = UpdateCallStatusRequestDto(
                user = userId,
                status = status,
            ),
        )
    }

    override suspend fun updatePermits(
        dialogId: Long,
        userId: String,
        role: String?,
        addPermits: List<String>?,
        delPermits: List<String>?,
    ) {
        callApiService.updatePermits(
            dialogId = dialogId,
            request = UpdateCallPermitsRequestDto(
                user = userId,
                role = role,
                addPermits = addPermits,
                delPermits = delPermits,
            ),
        )
    }

    override suspend fun updateState(
        dialogId: Long,
        userId: String,
        mediaSessionId: String,
        state: CallSessionState,
    ) {
        callApiService.updateState(
            dialogId = dialogId,
            request = UpdateCallStateRequestDto(
                user = userId,
                mediaSessionId = mediaSessionId,
                state = state.toDto(),
            ),
        )
    }

    override suspend fun stopCall(callId: Long) {
        callApiService.stopCall(callId)
    }
}
