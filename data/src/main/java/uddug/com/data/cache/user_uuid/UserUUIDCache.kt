package uddug.com.data.cache.user_uuid

import android.content.SharedPreferences
import com.google.gson.Gson
import uddug.com.data.cache.base.SharedPreferencesCache
import uddug.com.data.utils.fromJson
import toothpick.InjectConstructor

@InjectConstructor
class UserUUIDCache(private val gson: Gson, preferences: SharedPreferences) :
    SharedPreferencesCache<String>(preferences) {

    override val entityKey: String = "user_uuid"

    override fun toJson(entity: String): String = gson.toJson(entity)

    override fun fromJson(entityJson: String): String = gson.fromJson(entityJson)

}