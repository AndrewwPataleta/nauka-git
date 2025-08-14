package uddug.com.naukoteka.di.modules

import android.content.Context
import androidx.fragment.app.FragmentActivity
import toothpick.config.Module

class ActivityModule(activity: FragmentActivity) : Module() {

    init {
        bind(Context::class.java).toInstance(activity)
    }
}