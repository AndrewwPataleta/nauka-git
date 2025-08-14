package uddug.com.data.cache.base

import android.content.SharedPreferences

abstract class SharedPreferencesCache<T>(private val sharedPreferences: SharedPreferences) {

    protected abstract val entityKey: String

    protected abstract fun toJson(entity: T): String
    protected abstract fun fromJson(entityJson: String): T

    var entity: T?
        get() {
            val cachedEntity = sharedPreferences.getString(entityKey, null)
            return if (cachedEntity != null) {
                fromJson(cachedEntity)
            } else {
                null
            }
        }
        set(value) {
            if (value == null) {
                throw IllegalArgumentException("Could not set null. Use SharedPreferences#clear() method instead")
            }

            sharedPreferences.edit().putString(entityKey, toJson(value)).apply()
        }

    val requireEntity : T get() = entity!!

    fun clear() = sharedPreferences.edit().remove(entityKey).apply()

}