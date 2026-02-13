package uddug.com.naukoteka.environment

import android.content.Context
import android.content.SharedPreferences
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import toothpick.InjectConstructor

@InjectConstructor
class EnvironmentSwitcherService(
    context: Context
) {

    private val preferences: SharedPreferences =
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    private val _environmentFlow = MutableStateFlow(getCurrentEnvironment())
    val environmentFlow: StateFlow<AppEnvironment> = _environmentFlow

    private val environmentChangesRelay: PublishRelay<AppEnvironment> = PublishRelay.create()

    fun observeEnvironmentChanges(): Observable<AppEnvironment> = environmentChangesRelay.hide()

    fun getCurrentEnvironment(): AppEnvironment {
        val value = preferences.getString(KEY_ENVIRONMENT, null)
        return AppEnvironment.entries.firstOrNull { it.key == value } ?: DEFAULT_ENVIRONMENT
    }

    fun updateEnvironment(environment: AppEnvironment) {
        preferences.edit().putString(KEY_ENVIRONMENT, environment.key).apply()
        _environmentFlow.value = environment
        environmentChangesRelay.accept(environment)
    }

    companion object {
        private const val PREFERENCES_NAME = "environment_switcher"
        private const val KEY_ENVIRONMENT = "selected_environment"
        private val DEFAULT_ENVIRONMENT = AppEnvironment.DEV
    }
}

enum class AppEnvironment(
    val key: String,
    val apiBaseUrl: String,
) {
    DEV(
        key = "dev",
        apiBaseUrl = "https://stage.naukotheka.ru/api/",
    ),
    PROD(
        key = "prod",
        apiBaseUrl = "https://naukotheka.ru/api/",
    ),
}

