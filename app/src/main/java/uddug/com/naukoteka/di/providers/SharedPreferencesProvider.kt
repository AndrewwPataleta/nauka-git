package uddug.com.naukoteka.di.providers

import android.content.Context
import android.content.SharedPreferences
import toothpick.InjectConstructor
import javax.inject.Provider

@InjectConstructor
class SharedPreferencesProvider(
    private val context: Context
) : Provider<SharedPreferences> {
    override fun get(): SharedPreferences =
        context.getSharedPreferences("sharedprefs", Context.MODE_PRIVATE)
}