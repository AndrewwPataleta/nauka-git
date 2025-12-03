package uddug.com.data.mapper

import uddug.com.data.services.models.response.call.CallParticipantDto
import uddug.com.data.services.models.response.call.CallParticipantStateDto
import uddug.com.data.services.models.response.call.CallSessionStateDto
import uddug.com.domain.entities.call.CallParticipant
import uddug.com.domain.entities.call.CallParticipantState
import uddug.com.domain.entities.call.CallSessionState

fun CallParticipantDto.toDomain(): CallParticipant = CallParticipant(
    id = id,
    callId = call,
    userId = user,
    status = status,
    states = states.map { it.toDomain() },
    roles = roles,
    permits = permits,
)

fun CallParticipantStateDto.toDomain(): CallParticipantState = CallParticipantState(
    id = id,
    callId = call,
    userId = user,
    mediaSessionId = mediaSessionId,
    state = state.toDomain(),
)

fun CallSessionStateDto.toDomain(): CallSessionState = CallSessionState(
    micOn = micOn,
    camOn = camOn,
    handUp = handUp,
    sharingScreen = sharingScreen,
)

fun CallSessionState.toDto(): CallSessionStateDto = CallSessionStateDto(
    micOn = micOn,
    camOn = camOn,
    handUp = handUp,
    sharingScreen = sharingScreen,
)
