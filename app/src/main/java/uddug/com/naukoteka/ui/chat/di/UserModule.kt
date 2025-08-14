package uddug.com.naukoteka.ui.chat.di

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import retrofit2.Retrofit
import uddug.com.data.cache.user_id.UserIdCache
import uddug.com.data.cache.user_uuid.UserUUIDCache
import uddug.com.data.repositories.chat.ChatRepository
import uddug.com.data.repositories.chat.ChatRepositoryImpl
import uddug.com.data.repositories.user_profile.UserProfileMapper
import uddug.com.data.repositories.user_profile.UserProfileRepositoryImpl
import uddug.com.data.services.UserProfileApiService
import uddug.com.data.services.chat.ChatApiService
import uddug.com.domain.repositories.user_profile.UserProfileRepository

@Module
@InstallIn(ViewModelComponent::class)
object UserModule {

    @Provides
    fun provideUserApiService(retrofit: Retrofit): UserProfileApiService {
        return retrofit.create(UserProfileApiService::class.java)
    }

    @Provides
    fun provideUserIdCache(sharedPreferences: SharedPreferences): UserIdCache {
        return UserIdCache(gson = Gson(), preferences = sharedPreferences)
    }

    @Provides
    fun provideUserUserUUIDCache(sharedPreferences: SharedPreferences): UserUUIDCache {
        return UserUUIDCache(gson = Gson(), preferences = sharedPreferences)
    }


    @Provides
    fun provideUserProfileMapper(userIdCache: UserIdCache): UserProfileMapper {
        return UserProfileMapper(userIdCache)
    }


    @Provides
    fun provideUserRepository(
        apiService: UserProfileApiService,
        userProfileMapper: UserProfileMapper,
        userIdCache: UserUUIDCache,
    ): UserProfileRepository {
        return UserProfileRepositoryImpl(apiService, userProfileMapper, userIdCache)
    }
}
