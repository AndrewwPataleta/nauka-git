package uddug.com.naukoteka.ui.call.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import retrofit2.Retrofit
import uddug.com.data.repositories.call.CallRepositoryImpl
import uddug.com.data.services.CallApiService
import uddug.com.domain.repositories.call.CallRepository

@Module
@InstallIn(ViewModelComponent::class)
object CallModule {

    @Provides
    fun provideCallApiService(retrofit: Retrofit): CallApiService {
        return retrofit.create(CallApiService::class.java)
    }

    @Provides
    fun provideCallRepository(apiService: CallApiService): CallRepository {
        return CallRepositoryImpl(apiService)
    }
}
