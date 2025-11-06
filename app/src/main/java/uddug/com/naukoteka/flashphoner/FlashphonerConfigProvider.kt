package uddug.com.naukoteka.flashphoner

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import uddug.com.naukoteka.R

@Singleton
class FlashphonerConfigProvider @Inject constructor(
    @ApplicationContext private val appContext: Context
) {
    val defaultConfig: FlashphonerConfig
        get() = FlashphonerConfig(
            serverUrl = appContext.getString(R.string.flashphoner_default_server),
            streamName = appContext.getString(R.string.flashphoner_default_stream)
        )
}
