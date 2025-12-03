package uddug.com.domain.repositories.call

import uddug.com.domain.entities.call.CallParticipant
import uddug.com.domain.entities.call.CallSessionState

interface CallRepository {

    suspend fun getParticipants(dialogId: Long): List<CallParticipant>

    suspend fun updateStatus(dialogId: Long, userId: String, status: Int)

    suspend fun updatePermits(
        dialogId: Long,
        userId: String,
        role: String? = null,
        addPermits: List<String>? = null,
        delPermits: List<String>? = null,
    )

    suspend fun updateState(
        dialogId: Long,
        userId: String,
        mediaSessionId: String,
        state: CallSessionState,
    )

    suspend fun stopCall(callId: Long)
}
