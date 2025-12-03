package uddug.com.naukoteka.global.base

import android.app.ActionBar
import android.os.Bundle
import android.view.View
import androidx.viewbinding.ViewBinding
import uddug.com.naukoteka.NaukotekaApplication
import uddug.com.naukoteka.di.DI
import uddug.com.naukoteka.di.modules.ActivityModule
import moxy.MvpAppCompatActivity
import toothpick.Scope
import toothpick.ktp.KTP

abstract class BaseActivity : MvpAppCompatActivity() {

    protected abstract val contentView: ViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        val scope = getScope()
        scope
                .installModules(ActivityModule(this))
                .inject(this)
        super.onCreate(savedInstanceState)
        setContentView(contentView.root)
    }

    open fun getScope(): Scope {
        val appScope = (application as NaukotekaApplication).scope

        return KTP.openRootScope()
            .openSubScope(appScope.name)
            .openSubScope(DI.MAIN_ACTIVITY_SCOPE)
    }

    override fun onDestroy() {
        super.onDestroy()
        KTP.closeScope(DI.MAIN_ACTIVITY_SCOPE)
    }

    fun hideStatusBar() {
        val decorView = window.decorView
        val uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN
        decorView.systemUiVisibility = uiOptions
        val actionBar: ActionBar? = actionBar
        actionBar?.hide()
    }
}
