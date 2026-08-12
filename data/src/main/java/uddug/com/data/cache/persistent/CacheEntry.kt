package uddug.com.data.cache.persistent

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Обобщённая строка persistent-кэша: под одним строковым ключом храним
 * произвольный JSON (сериализованный Gson доменный список) + время записи.
 * Никакие доменные поля не размазываются по колонкам — одна таблица на всё.
 */
@Entity(tableName = "cache_entries")
data class CacheEntry(
    @PrimaryKey val key: String,
    val json: String,
    val updatedAt: Long,
)
