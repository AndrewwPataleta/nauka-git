package uddug.com.naukoteka

import android.app.Application
import android.content.res.Configuration
import com.franmontiel.localechanger.LocaleChanger
import dagger.hilt.android.HiltAndroidApp
import uddug.com.naukoteka.di.DI
import uddug.com.naukoteka.di.modules.AppModule
import uddug.com.naukoteka.ext.data.SUPPORTED_LOCALES
import toothpick.Scope
import toothpick.ktp.KTP

@HiltAndroidApp
class NaukotekaApplication : Application() {

    lateinit var scope: Scope

    override fun onCreate() {
        super.onCreate()
        initializeToothpick()
        LocaleChanger.initialize(this, SUPPORTED_LOCALES)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        LocaleChanger.onConfigurationChanged()
    }

    private fun initializeToothpick() {
        scope = KTP.openRootScope()
            .openSubScope(DI.APP_SCOPE)
            .installModules(AppModule(this))
    }

}