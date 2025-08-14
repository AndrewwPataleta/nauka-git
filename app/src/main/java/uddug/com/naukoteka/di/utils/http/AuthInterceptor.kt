package uddug.com.naukoteka.di.utils.http

import okhttp3.Interceptor
import toothpick.InjectConstructor


@InjectConstructor
class AuthInterceptor(
) : Interceptor {

    private val accessToken = ""

    override fun intercept(chain: Interceptor.Chain) = chain.run {
        val bearerToken = request().header("Authorization") ?: "Bearer $accessToken"
        proceed(
            request().newBuilder()
                .removeHeader("Authorization")
                .addHeader("Authorization", bearerToken)
                .build()
        )
    }
}
