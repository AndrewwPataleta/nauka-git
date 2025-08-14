import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import androidx.test.core.app.ApplicationProvider
import com.google.gson.Gson
import okhttp3.OkHttpClient
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.notNullValue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import retrofit2.Retrofit
import toothpick.Toothpick
import uddug.com.data.NaukotekaCookieJar
import uddug.com.data.services.AuthApiService
import uddug.com.data.services.UserProfileApiService
import uddug.com.domain.SchedulersProvider
import uddug.com.domain.repositories.auth.AuthRepository
import uddug.com.domain.repositories.user_profile.UserProfileRepository
import uddug.com.domain.utils.logging.ILogger
import uddug.com.naukoteka.di.DI
import uddug.com.naukoteka.di.modules.AppModule
import uddug.com.naukoteka.navigation.AppRouter
import uddug.com.naukoteka.utils.text.isNotNullOrEmpty


@RunWith(RobolectricTestRunner::class)
class ApplicationModuleTest {

    private val getAppScope = Toothpick.openRootScope()
        .openSubScope(DI.APP_SCOPE)

    @Test
    @Throws(Exception::class)
    fun testModule_shouldReturnApplicationBindings() {
        // GIVEN
        val application: Application = ApplicationProvider.getApplicationContext()
        val appScope = getAppScope.installTestModules(AppModule(application))

        // WHEN
        val injectedApp = appScope.getInstance(Context::class.java)

        val logger = appScope.getInstance(ILogger::class.java)
        val sharedPreferences = appScope.getInstance(SharedPreferences::class.java)
        val gson = appScope.getInstance(Gson::class.java)
        val schedulers = appScope.getInstance(SchedulersProvider::class.java)
        val serverUrl = appScope.getInstance(String::class.java, "uddug.com.naukoteka.di.ServerUrl")
        val cookieJar = appScope.getInstance(NaukotekaCookieJar::class.java)
        val okkHttp = appScope.getInstance(OkHttpClient::class.java)
        val retrofit = appScope.getInstance(Retrofit::class.java)
        val appRouter = appScope.getInstance(AppRouter::class.java)

        val authApiService = appScope.getInstance(AuthApiService::class.java)
        val userProfileApiService = appScope.getInstance(UserProfileApiService::class.java)

        val authRepository = appScope.getInstance(AuthRepository::class.java)
        val userProfileRepository = appScope.getInstance(UserProfileRepository::class.java)

        // THEN
        assertThat("injected app is instance of context ", injectedApp is Context)
        assertThat(logger, notNullValue())
        assertThat(sharedPreferences, notNullValue())
        assertThat(gson, notNullValue())
        assertThat(schedulers, notNullValue())
        assertThat("check server url is not empty or null", serverUrl.isNotNullOrEmpty())
        assertThat(cookieJar, notNullValue())
        assertThat(okkHttp, notNullValue())
        assertThat(retrofit, notNullValue())
        assertThat(appRouter, notNullValue())
        assertThat(authApiService, notNullValue())
        assertThat(userProfileApiService, notNullValue())
        assertThat(authRepository, notNullValue())
        assertThat(userProfileRepository, notNullValue())
    }

    @Test
    @Throws(Exception::class)
    fun testModule_shouldReturnDefaultSharedPreferences() {
        // GIVEN
        val application = ApplicationProvider.getApplicationContext<Application>()
        val itemKey = "isValid"
        val appScope = getAppScope.installModules(AppModule(application))



        // WHEN
        val sharedPreferencesFromScope = appScope.getInstance(
            SharedPreferences::class.java
        )
        sharedPreferencesFromScope.edit().putBoolean(itemKey, true).commit()

        // THEN
        assertThat(sharedPreferencesFromScope.getBoolean(itemKey, false), `is`(true))
    }
}
