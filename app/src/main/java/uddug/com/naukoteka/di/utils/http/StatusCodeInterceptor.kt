package uddug.com.naukoteka.di.utils.http

import android.content.Context
import android.content.Intent
import uddug.com.domain.utils.logging.ILogger
import uddug.com.naukoteka.utils.getApiError
import okhttp3.Interceptor
import okhttp3.Response
import toothpick.InjectConstructor
import uddug.com.domain.entities.StatusCode
import uddug.com.naukoteka.ui.activities.DeepLinkActivity

@InjectConstructor
class StatusCodeParsingInterceptor constructor(
    private val logger: ILogger,
    private val context: Context
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.run { proceed(request()) }
        try {
            val statusCode = response.getApiError(context)?.code?:0
            logger.info("Parsed status code: $statusCode")

            response.javaClass.getDeclaredField("code").run {
                isAccessible = true
                set(response, statusCode)
            }

        } catch (e: Exception) {
            logger.error("Could not set parsed status code", e)
            return response
        }

        return response
    }
}