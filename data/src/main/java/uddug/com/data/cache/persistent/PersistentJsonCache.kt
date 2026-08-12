package uddug.com.data.cache.persistent

import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.reflect.Type
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Обобщённый persistent JSON-кэш поверх Room. Сериализует доменные списки в JSON
 * через собственный Gson (с адаптером Instant <-> epoch millis, т.к. доменные
 * модели чата содержат java.time.Instant). Все обращения к Room/Gson обёрнуты в
 * runCatching: сбой кэша НИКОГДА не роняет вызывающий код — просто miss (null).
 *
 * Это отдельный Gson-инстанс только для кэша; другие Gson в приложении не трогаем.
 */
@Singleton
class PersistentJsonCache @Inject constructor(
    private val dao: CacheDao,
) {

    private val gson = GsonBuilder()
        .registerTypeAdapter(
            Instant::class.java,
            JsonSerializer<Instant> { src, _, _ -> JsonPrimitive(src.toEpochMilli()) },
        )
        .registerTypeAdapter(
            Instant::class.java,
            JsonDeserializer { json, _, _ -> Instant.ofEpochMilli(json.asLong) },
        )
        .create()

    /**
     * Возвращает закэшированный список под [key] или null при промахе/ошибке.
     * [type] — java.lang.reflect.Type целевого List<T> (например через TypeToken).
     */
    suspend fun <T> getList(key: String, type: Type): List<T>? = withContext(Dispatchers.IO) {
        runCatching {
            val entry = dao.get(key) ?: return@runCatching null
            gson.fromJson<List<T>>(entry.json, type)
        }.getOrNull()
    }

    /** Кладёт [value] под [key]. Ошибка сериализации/записи молча проглатывается. */
    suspend fun putList(key: String, value: List<*>) {
        withContext(Dispatchers.IO) {
            runCatching {
                val json = gson.toJson(value)
                dao.put(CacheEntry(key = key, json = json, updatedAt = System.currentTimeMillis()))
            }
        }
    }

    /** Возвращает закэшированный ОБЪЕКТ под [key] или null при промахе/ошибке. */
    suspend fun <T> getObject(key: String, type: Type): T? = withContext(Dispatchers.IO) {
        runCatching {
            val entry = dao.get(key) ?: return@runCatching null
            gson.fromJson<T>(entry.json, type)
        }.getOrNull()
    }

    /** Кладёт произвольный объект [value] под [key]. Ошибки молча проглатываются. */
    suspend fun putObject(key: String, value: Any) {
        withContext(Dispatchers.IO) {
            runCatching {
                val json = gson.toJson(value)
                dao.put(CacheEntry(key = key, json = json, updatedAt = System.currentTimeMillis()))
            }
        }
    }
}
