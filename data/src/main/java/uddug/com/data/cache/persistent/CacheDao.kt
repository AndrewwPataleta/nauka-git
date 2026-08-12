package uddug.com.data.cache.persistent

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CacheDao {

    @Query("SELECT * FROM cache_entries WHERE `key` = :key LIMIT 1")
    suspend fun get(key: String): CacheEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun put(entry: CacheEntry)

    @Query("DELETE FROM cache_entries WHERE `key` = :key")
    suspend fun delete(key: String)
}
