package uddug.com.naukoteka.di.utils.http

import android.content.Context
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import toothpick.InjectConstructor
import uddug.com.domain.utils.logging.ILogger
import uddug.com.naukoteka.BuildConfig

@InjectConstructor
class UserAgentInterceptor constructor(

) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request: Request = chain.request().newBuilder().addHeader("User-Agent", "Android:${BuildConfig.VERSION_CODE}/nkt").build()
        return chain.proceed(request)
    }
}