package uddug.com.naukoteka.di.providers

import uddug.com.naukoteka.di.ServerUrl
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import toothpick.InjectConstructor
import javax.inject.Provider

@InjectConstructor
class RetrofitProvider(
    @ServerUrl private val serverUrl: String,
    private val okHttpClient: OkHttpClient,
) : Provider<Retrofit> {
    override fun get(): Retrofit =
        Retrofit.Builder()
            .baseUrl(serverUrl)
            .client(okHttpClient)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
}