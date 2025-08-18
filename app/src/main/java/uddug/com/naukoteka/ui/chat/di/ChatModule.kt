package uddug.com.naukoteka.ui.chat.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import retrofit2.Retrofit
import uddug.com.data.cache.cookies.CookiesCache
import uddug.com.data.repositories.chat.ChatRepositoryImpl
import uddug.com.data.services.chat.ChatApiService
import uddug.com.domain.interactors.chat.ChatInteractor
import uddug.com.domain.repositories.chat.ChatRepository

@Module
@InstallIn(ViewModelComponent::class)
object ChatModule {

    @Provides
    fun provideChatApiService(retrofit: Retrofit): ChatApiService {
        return retrofit.create(ChatApiService::class.java)
    }


    @Provides
    fun provideChatRepository(apiService: ChatApiService): ChatRepository = ChatRepositoryImpl(apiService)

    @Provides
    fun provideChatInteractor(chatRepository: ChatRepository): ChatInteractor = ChatInteractor(chatRepository)

    @Provides
    fun provideSocketService(cookiesCache: CookiesCache): SocketService {
        return SocketServiceImpl(cookiesCache)
    }

}
