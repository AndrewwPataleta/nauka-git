package uddug.com.naukoteka.mvvm.call

import android.content.SharedPreferences
import com.google.gson.Gson
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActiveCallStore @Inject constructor(
    private val sharedPreferences: SharedPreferences,
) {

    private val gson = Gson()

    fun save(state: ActiveCallState) {
        sharedPreferences.edit()
            .putString(KEY_ACTIVE_CALL, gson.toJson(state))
            .apply()
    }

    fun get(): ActiveCallState? {
        val raw = sharedPreferences.getString(KEY_ACTIVE_CALL, null) ?: return null
        return runCatching { gson.fromJson(raw, ActiveCallState::class.java) }.getOrNull()
    }

    fun clear() {
        sharedPreferences.edit().remove(KEY_ACTIVE_CALL).apply()
    }

    companion object {
        private const val KEY_ACTIVE_CALL = "active_call_state"
    }
}

data class ActiveCallState(
    val dialogId: Long,
    val contactName: String?,
    val avatarUrl: String?,
    val callTitle: String?,
    val isVideoCall: Boolean,
    val isGroupCall: Boolean,
)
