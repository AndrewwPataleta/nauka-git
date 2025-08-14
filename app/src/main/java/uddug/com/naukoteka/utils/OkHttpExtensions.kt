package uddug.com.naukoteka.utils

import android.content.Context
import android.content.Intent
import androidx.core.os.bundleOf
import com.google.gson.Gson
import uddug.com.data.utils.fromJson
import uddug.com.domain.entities.ApiErrorDetail
import uddug.com.domain.entities.ApiErrorWrapper
import uddug.com.domain.entities.StatusCode
import okhttp3.FormBody
import okhttp3.Response
import uddug.com.naukoteka.navigation.Screens.TO_LOGIN
import uddug.com.naukoteka.ui.activities.DeepLinkActivity
import uddug.com.naukoteka.ui.activities.main.AuthActivity
import java.nio.charset.Charset

inline fun Any.formBody(func: FormBody.Builder.() -> FormBody.Builder): FormBody {
    return FormBody.Builder().func().build()
}

/*fun Response.getStatusCode(): Int {
    val responseBody = body!!
    val source = responseBody.source().also { it.request(Long.MAX_VALUE) }
    val responseBodyString = source.buffer().clone().readString(Charset.forName("UTF-8"))

    return Gson().fromJson<ApiResponse<Any>>(responseBodyString).statusCode.code
}*/

fun Response.getApiError(context: Context): ApiErrorDetail {
    val responseBody = body!!
    val source = responseBody.source().also { it.request(Long.MAX_VALUE) }

    val responseBodyString = source.buffer().clone().readString(Charset.forName("UTF-8"))
    return try {
        Gson().fromJson<ApiErrorWrapper>(responseBodyString).apiErrorDetail
    } catch (npe: Exception) {
        val message = when (StatusCode.values().find { it.code == code }) {
            StatusCode.Unauthorized -> {
                val intent = Intent(context, AuthActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtras(
                    bundleOf(
                        TO_LOGIN to true
                    )
                )
                context.startActivity(intent)
                "Auth error"
            }

            StatusCode.NoConnection -> "No connection"
            StatusCode.BadRequest -> "Bad request"
            StatusCode.Forbidden -> "Forbidden method"
            StatusCode.NotFound -> "NotFound method"
            StatusCode.InternalServerError -> "Internal Server Error"
            else -> "Could not found status code error"
        }
        ApiErrorDetail(code, "", "", message)
    }
}