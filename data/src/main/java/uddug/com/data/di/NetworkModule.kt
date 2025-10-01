package uddug.com.data.di

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import uddug.com.data.NaukotekaCookieJar
import uddug.com.data.cache.cookies.CookiesCache
import dagger.hilt.android.qualifiers.ApplicationContext
import uddug.com.data.BuildConfig
import okhttp3.logging.HttpLoggingInterceptor

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    fun provideSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences("sharedprefs", Context.MODE_PRIVATE)
    }

    @Provides
    fun provideGson(): Gson {
        return Gson()
    }

    @Provides
    fun provideCookiesCache(gson: Gson, preferences: SharedPreferences): CookiesCache {
        return CookiesCache(gson, preferences)
    }

    @Provides
    fun provideNaukotekaCookieJar(cookiesCache: CookiesCache): NaukotekaCookieJar {
        return NaukotekaCookieJar(cookiesCache)
    }

    @Provides
    fun provideOkHttpClient(cookieJar: NaukotekaCookieJar): OkHttpClient {
        return OkHttpClient.Builder()
            .cookieJar(cookieJar)
            .addInterceptor(UserAgentInterceptor())
            .addNetworkInterceptor(HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG) {
                    HttpLoggingInterceptor.Level.BODY
                } else {
                    HttpLoggingInterceptor.Level.NONE
                }
            })
            
            .connectTimeout(2, TimeUnit.MINUTES)
            .readTimeout(2, TimeUnit.MINUTES)
            .writeTimeout(2, TimeUnit.MINUTES)
            .build()
    }

    @Provides
    fun provideRetrofit( okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://stage.naukotheka.ru/api/")
            .client(okHttpClient)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
