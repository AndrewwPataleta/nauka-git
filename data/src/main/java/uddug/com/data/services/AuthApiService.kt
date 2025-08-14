package uddug.com.data.services

import io.reactivex.Completable
import io.reactivex.Single
import uddug.com.data.services.models.request.auth.*
import uddug.com.data.services.models.response.auth.*
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApiService {

    @POST("a/login-actions/authenticate")
    fun login(@Body authRequestDto: AuthRequestDto): Single<AuthResponseDto>

    @POST("a/login-actions/register")
    fun register(@Body authRequestDto: RegisterRequestDto): Single<AuthResponseDto>

    @POST("a/login-actions/action-token")
    fun checkTokenKey(@Body checkTokenKey: CheckTokenKeyDto): Single<CheckTokenKeyResponseDto>

    @POST("a/login-actions/authenticate/check-email-free")
    fun checkEmailFree(@Body checkEmailFreeDto: CheckEmailFreeDto): Single<EmailFreeResponseDto>

    @GET("a/login-actions/authenticate/idp-hints")
    fun getSocialTypes(): Single<SocialTypesResponseDto>

    @POST("a/login-actions/authenticate")
    fun authenticate(@Body authRequestDto: AuthRequestDto): Single<AuthResponseDto>

    @POST("a/login-actions/execute")
    fun passwordRecovery(@Body resetRequestDto: ResetRequestDto): Completable


}