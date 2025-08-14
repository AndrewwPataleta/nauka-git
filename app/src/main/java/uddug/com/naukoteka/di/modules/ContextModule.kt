package uddug.com.naukoteka.di.modules

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object ContextModule {

    @Provides
    fun provideContext(@ApplicationContext application: Context): Context {
        return application
    }
}
