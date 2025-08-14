package uddug.com.data.di

import android.content.Context
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import toothpick.InjectConstructor

@InjectConstructor
class UserAgentInterceptor constructor(

) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request: Request = chain.request().newBuilder().addHeader("User-Agent", "Android:17809/nkt").build()
        return chain.proceed(request)
    }
}