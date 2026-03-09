package uddug.com.naukoteka.mvvm.call

import android.content.SharedPreferences
import com.google.gson.Gson
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IncomingCallStore @Inject constructor(
    private val sharedPreferences: SharedPreferences,
) {

    private val gson = Gson()

    fun save(event: IncomingCallEvent) {
        sharedPreferences.edit()
            .putString(KEY_PENDING_INCOMING_CALL, gson.toJson(event))
            .apply()
    }

    fun get(): IncomingCallEvent? {
        val raw = sharedPreferences.getString(KEY_PENDING_INCOMING_CALL, null) ?: return null
        return runCatching { gson.fromJson(raw, IncomingCallEvent::class.java) }.getOrNull()
    }

    fun clear() {
        sharedPreferences.edit().remove(KEY_PENDING_INCOMING_CALL).apply()
    }

    companion object {
        private const val KEY_PENDING_INCOMING_CALL = "pending_incoming_call"
    }
}
