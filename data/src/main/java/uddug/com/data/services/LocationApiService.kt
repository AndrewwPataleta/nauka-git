package uddug.com.data.services

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.internal.operators.completable.CompletableAmb
import okhttp3.MultipartBody
import okhttp3.RequestBody
import uddug.com.data.services.models.request.user_profile.NickNameCheckRequestDto
import uddug.com.data.services.models.request.user_profile.UserProfileRequestDto
import uddug.com.data.services.models.response.user_profile.CheckNickNameResponseDto
import uddug.com.data.services.models.response.user_profile.UserProfileDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import uddug.com.data.services.models.request.country.FindSettlementRequest
import uddug.com.data.services.models.request.user_profile.NickNameChangeRequestDto
import uddug.com.data.services.models.request.user_profile.UserProfileShortRequestDto
import uddug.com.data.services.models.response.auth.AuthResponseDto
import uddug.com.data.services.models.response.country.CountryDto
import uddug.com.data.services.models.response.country.SettlementDto
import uddug.com.data.services.models.response.user_profile.UserProfileFullInfoDto
import uddug.com.domain.entities.country.Country

interface LocationApiService {

    @GET("core/cls/all")
    fun getCounties(
        @Query("orderBy") orderBy: List<String> = listOf(
            "level",
            "term"
        ),
        @Query("cls") cls: Int = 208,
        @Query("lang") lang: Int = 1,
        @Query("pageSize") pageSize: Int = 500
    ): Single<List<CountryDto>>


    @POST("core/address/find_city")
    fun findSettlementsByCountries(
        @Body body: FindSettlementRequest,
    ): Single<List<SettlementDto>>

}
