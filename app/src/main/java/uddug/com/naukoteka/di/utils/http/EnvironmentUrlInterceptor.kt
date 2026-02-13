package uddug.com.naukoteka.di.utils.http

import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.HttpUrl.Companion.toHttpUrl
import toothpick.InjectConstructor
import uddug.com.naukoteka.environment.EnvironmentSwitcherService

@InjectConstructor
class EnvironmentUrlInterceptor(
    private val environmentSwitcherService: EnvironmentSwitcherService,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val currentEnvironment = environmentSwitcherService.getCurrentEnvironment()
        val baseUrl = currentEnvironment.apiBaseUrl.toHttpUrl()
        val originalUrl = request.url

        val newUrl = originalUrl.newBuilder()
            .scheme(baseUrl.scheme)
            .host(baseUrl.host)
            .port(baseUrl.port)
            .build()

        val updatedRequest = request.newBuilder()
            .url(newUrl)
            .build()

        return chain.proceed(updatedRequest)
    }
}

