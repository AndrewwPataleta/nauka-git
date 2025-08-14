package uddug.com.data.repositories.auth

import uddug.com.data.services.models.response.auth.CheckTokenKeyResponseDto
import uddug.com.domain.repositories.auth.models.SessionAttributes
import toothpick.InjectConstructor

@InjectConstructor
class AuthRepositoryMapper {

    fun mapSessionAttributesToDomain(dto: CheckTokenKeyResponseDto) = dto.run {
        SessionAttributes(
            authSessionId = authSessionId!!,
            clientId = clientId!!,
            key = key!!,
            tabId = tabId!!
        )
    }

}