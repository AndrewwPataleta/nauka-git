package uddug.com.data.cache.first_launch

import android.content.SharedPreferences
import com.google.gson.Gson
import uddug.com.data.cache.base.DefaultSharedPreferencesCache
import uddug.com.data.utils.fromJson
import toothpick.InjectConstructor

@InjectConstructor
class FirstLaunchCache(private val gson: Gson, preferences: SharedPreferences) :
    DefaultSharedPreferencesCache<Boolean>(preferences) {

    override fun defaultValue() = true

    override val entityKey: String = "isFirstLaunch"

    override fun toJson(entity: Boolean): String = gson.toJson(entity)

    override fun fromJson(entityJson: String): Boolean = gson.fromJson(entityJson)

}