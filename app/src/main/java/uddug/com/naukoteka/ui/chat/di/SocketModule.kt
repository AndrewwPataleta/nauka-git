package uddug.com.naukoteka.ui.chat.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import uddug.com.data.cache.cookies.CookiesCache

@Module
@InstallIn(SingletonComponent::class)
object SocketModule {

    @Provides
    @Singleton
    fun provideSocketService(cookiesCache: CookiesCache): SocketService {
        return SocketServiceImpl(cookiesCache)
    }
}
