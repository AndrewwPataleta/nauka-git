package uddug.com.data.cache.persistent

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PersistentCacheModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, "nkt_cache.db")
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideCacheDao(database: AppDatabase): CacheDao = database.cacheDao()
}
