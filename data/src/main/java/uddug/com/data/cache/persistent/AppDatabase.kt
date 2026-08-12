package uddug.com.data.cache.persistent

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [CacheEntry::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cacheDao(): CacheDao
}
