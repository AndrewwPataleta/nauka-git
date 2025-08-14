package com.nauchat.core.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import com.nauchat.domain.dispatcher.UseCaseDispatchers
import com.nauchat.domain.dispatcher.ViewModelDispatchers
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module(includes = [DataModule::class])
class CoreModule {

    @Singleton
    @Provides
    fun provideCoroutineScope() = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())

    @Provides
    fun provideUseCaseDispatchers(): UseCaseDispatchers {
        return UseCaseDispatchers(Dispatchers.IO, Dispatchers.Default, Dispatchers.Main)
    }

    @Provides
    fun provideViewModelDispatchers(): ViewModelDispatchers {
        return ViewModelDispatchers(Dispatchers.IO, Dispatchers.Default, Dispatchers.Main)
    }


}
