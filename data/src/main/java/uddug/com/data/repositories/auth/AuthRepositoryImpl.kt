package uddug.com.data.repositories.auth

import io.reactivex.Completable
import io.reactivex.Single
import uddug.com.data.cache.cookies.CookiesCache
import uddug.com.data.cache.user_id.UserIdCache
import uddug.com.data.services.AuthApiService
import uddug.com.data.services.models.request.auth.*
import uddug.com.domain.entities.EmailNotFreeException
import uddug.com.domain.repositories.auth.AuthRepository
import uddug.com.domain.repositories.auth.models.SessionAttributes
import uddug.com.domain.repositories.auth.models.SocialType
import toothpick.InjectConstructor

@InjectConstructor
class AuthRepositoryImpl(
    private val authApiService: AuthApiService,
    private val mapper: AuthRepositoryMapper,
    private val cookiesCache: CookiesCache,
    private val userIdCache: UserIdCache,
) : AuthRepository {


    override fun loginEmail(login: String, password: String): Completable {
        return authApiService.login(AuthRequestDto(login, password))
            .flatMapCompletable {

                Completable.complete()
            }
    }

    override fun sendLetterToRecoveryPassword(email: String): Completable {
        return authApiService.passwordRecovery(ResetRequestDto("reset-credentials", email))
    }

    override fun register(login: String, password: String): Completable {
        return authApiService.register(RegisterRequestDto(login, password))
            .flatMapCompletable {
                userIdCache.entity = it.userStatus.id
                Completable.complete()
            }
    }

    override fun checkConfirmRegistration(key: String): Completable {
        return authApiService.checkTokenKey(CheckTokenKeyDto(key))
            .flatMapCompletable { Completable.complete() }
    }

    override fun checkConfirmRecovery(key: String): Single<SessionAttributes> {
        return authApiService.checkTokenKey(CheckTokenKeyDto(key))
            .map { mapper.mapSessionAttributesToDomain(it) }
    }

    override fun setNewPassword(
        sessionAttributes: SessionAttributes,
        password: String
    ): Completable {
        return authApiService.checkTokenKey(
            sessionAttributes.run {
                CheckTokenKeyDto(key, clientId, tabId, authSessionId, password)
            }
        )
            .flatMapCompletable {
                if (!it.followUp) {
                    Completable.complete()
                } else {
                    throw Exception("Something went wrong during set new password")
                }
            }
    }

    override fun checkEmailForFree(email: String): Completable {
        return authApiService.checkEmailFree(CheckEmailFreeDto(email))
            .flatMapCompletable {
                if (it.emailIsFree) {
                    Completable.complete()
                } else {
                    throw EmailNotFreeException
                }
            }
    }

    override fun socialTypes(): Single<List<SocialType>> {
        return authApiService.getSocialTypes()
            .map {
                it.idpVariants.map { socialType ->
                    SocialType.values().find { it.raw == socialType }
                }.filterNotNull()
            }
    }

    override fun socialAuth(socialType: SocialType, key: String): Completable {
        return authApiService.authenticate(AuthRequestDto(socialLogin = socialType.raw, key = key))
            .flatMapCompletable {
                Completable.complete()
            }
    }


}