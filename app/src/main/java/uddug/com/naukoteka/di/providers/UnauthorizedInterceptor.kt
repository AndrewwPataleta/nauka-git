package uddug.com.naukoteka.di.providers

import android.content.Context
import android.content.Intent
import okhttp3.Interceptor
import java.io.IOException
import okhttp3.Response

import androidx.annotation.NonNull
import uddug.com.naukoteka.ui.activities.DeepLinkActivity


internal class UnauthorizedInterceptor(
    val context: Context
) : Interceptor {
    @Throws(IOException::class)
    override fun intercept(@NonNull chain: Interceptor.Chain): Response {
        val response: Response = chain.proceed(chain.request())
        if (response.code === 401) {
            context.startActivity(Intent(context, DeepLinkActivity::class.java))
        }
        return response
    }
}