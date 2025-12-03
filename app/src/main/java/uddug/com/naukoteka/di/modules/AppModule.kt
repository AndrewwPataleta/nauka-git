package uddug.com.naukoteka.di.modules

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.github.terrakok.cicerone.Cicerone
import com.github.terrakok.cicerone.NavigatorHolder
import com.google.gson.Gson
import uddug.com.data.NaukotekaCookieJar
import uddug.com.data.repositories.auth.AuthRepositoryImpl
import uddug.com.data.repositories.call.CallRepositoryImpl
import uddug.com.data.repositories.user_profile.UserProfileRepositoryImpl
import uddug.com.data.services.auth.AuthApiHolder
import uddug.com.data.services.AuthApiService
import uddug.com.data.services.CallApiService
import uddug.com.data.services.UserProfileApiService
import uddug.com.domain.SchedulersProvider
import uddug.com.domain.repositories.auth.AuthRepository
import uddug.com.domain.repositories.call.CallRepository
import uddug.com.domain.repositories.user_profile.UserProfileRepository
import uddug.com.domain.utils.logging.ILogger
import uddug.com.naukoteka.di.ServerUrl
import uddug.com.naukoteka.di.providers.*
import uddug.com.naukoteka.di.utils.Logger
import uddug.com.naukoteka.navigation.AppRouter
import uddug.com.naukoteka.ui.chat.di.SocketService
import uddug.com.naukoteka.ui.chat.di.SocketServiceImpl
import uddug.com.naukoteka.ui.chat.di.SocketServiceProvider
import okhttp3.CookieJar
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import toothpick.config.Module
import uddug.com.data.repositories.location.LocationRepositoryImpl
import uddug.com.data.services.LocationApiService
import uddug.com.domain.repositories.country.LocationRepository

class AppModule(application: Application) : Module() {

    init {
        bind(Context::class.java).toInstance(application)
        bind(ILogger::class.java).to(Logger::class.java).singleton()
        bind(SharedPreferences::class.java).toProvider(SharedPreferencesProvider::class.java)
        bind(Gson::class.java).toProvider(GsonProvider::class.java).singleton()
        bind(SchedulersProvider::class.java).toInstance(AppSchedulersProvider())

        bind(String::class.java).withName(ServerUrl::class.java)
            .toInstance("https://stage.naukotheka.ru/api/")
        bind(CookieJar::class.java).to(NaukotekaCookieJar::class.java)

        bind(OkHttpClient::class.java).toProvider(OkHttpProvider::class.java).singleton()
        bind(Retrofit::class.java).toProvider(RetrofitProvider::class.java).singleton()

        val cicerone = Cicerone.create(AppRouter())
        bind(AppRouter::class.java).toInstance(cicerone.router)
        bind(NavigatorHolder::class.java).toInstance(cicerone.getNavigatorHolder())


        bind(AuthApiService::class.java).toProvider(AuthApiProvider::class.java).singleton()
        bind(UserProfileApiService::class.java).toProvider(UserProfileApiProvider::class.java).singleton()
        bind(CallApiService::class.java).toProvider(CallApiProvider::class.java).singleton()
        bind(AuthApiHolder::class.java).singleton()
        bind(AuthRepository::class.java).to(AuthRepositoryImpl::class.java)
        bind(CallRepository::class.java).to(CallRepositoryImpl::class.java)
        bind(UserProfileRepository::class.java).to(UserProfileRepositoryImpl::class.java)
        bind(LocationApiService::class.java).toProvider(LocationApiProvider::class.java).singleton()
        bind(LocationRepository::class.java).to(LocationRepositoryImpl::class.java)
        bind(SocketService::class.java).toProvider(SocketServiceProvider::class.java).singleton()

    }


}
