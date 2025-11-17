package uddug.com.naukoteka

import android.app.Application
import android.content.res.Configuration
import com.franmontiel.localechanger.LocaleChanger
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import uddug.com.naukoteka.di.DI
import uddug.com.naukoteka.di.modules.AppModule
import uddug.com.naukoteka.ext.data.SUPPORTED_LOCALES
import uddug.com.naukoteka.flashphoner.FlashphonerEnvironment
import toothpick.Scope
import toothpick.ktp.KTP

@HiltAndroidApp
class NaukotekaApplication : Application() {

    lateinit var scope: Scope
    @Inject lateinit var flashphonerEnvironment: FlashphonerEnvironment

    override fun onCreate() {
        super.onCreate()
        initializeToothpick()
        LocaleChanger.initialize(this, SUPPORTED_LOCALES)
        flashphonerEnvironment.ensureInitialised()
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