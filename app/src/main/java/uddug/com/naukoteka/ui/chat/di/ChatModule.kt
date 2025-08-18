package uddug.com.naukoteka.ui.chat.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import retrofit2.Retrofit
import uddug.com.data.cache.cookies.CookiesCache
import uddug.com.data.services.chat.ChatApiService
import uddug.com.data.repositories.chat.ChatRepositoryImpl
import uddug.com.domain.repositories.chat.ChatRepository
import uddug.com.domain.interactors.chat.ChatInteractor

@Module
@InstallIn(ViewModelComponent::class)
object ChatModule {

    @Provides
    fun provideChatApiService(retrofit: Retrofit): ChatApiService {
        return retrofit.create(ChatApiService::class.java)
    }


    @Provides
    fun provideChatRepository(apiService: ChatApiService): ChatRepository {
        return ChatRepositoryImpl(apiService)
    }

    @Provides
    fun provideChatInteractor(repository: ChatRepository): ChatInteractor {
        return ChatInteractor(repository)
    }

    @Provides
    fun provideSocketService(cookiesCache: CookiesCache): SocketService {
        return SocketServiceImpl(cookiesCache)
    }

}
