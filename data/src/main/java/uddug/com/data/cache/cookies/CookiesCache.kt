package uddug.com.data.cache.cookies

import android.content.SharedPreferences
import com.google.gson.Gson
import uddug.com.data.cache.base.SharedPreferencesCache
import uddug.com.data.utils.fromJson
import toothpick.InjectConstructor

@InjectConstructor
class CookiesCache(private val gson: Gson, private val preferences: SharedPreferences) :
    SharedPreferencesCache<String>(preferences) {

    override val entityKey: String = "cookieCache"

    override fun toJson(entity: String): String = gson.toJson(entity)

    override fun fromJson(entityJson: String): String = gson.fromJson(entityJson)

    fun getAuthCookies(): String {
        return preferences.getString(entityKey,"")!!
    }
 }