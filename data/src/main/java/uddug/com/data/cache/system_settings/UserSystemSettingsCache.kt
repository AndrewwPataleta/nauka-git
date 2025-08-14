package uddug.com.data.cache.system_settings

import android.content.SharedPreferences
import com.google.gson.Gson
import uddug.com.data.cache.base.SharedPreferencesCache
import uddug.com.data.utils.fromJson
import toothpick.InjectConstructor
import uddug.com.data.cache.model.UserSystemSettings

@InjectConstructor
class UserSystemSettingsCache(private val gson: Gson, preferences: SharedPreferences) :
    SharedPreferencesCache<UserSystemSettings>(preferences) {

    override val entityKey: String = "user_system_settings_cache"
    override fun toJson(entity: UserSystemSettings): String {
        return gson.toJson(entity)
    }

    override fun fromJson(entityJson: String): UserSystemSettings = gson.fromJson(entityJson)

}