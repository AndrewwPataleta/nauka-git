package uddug.com.naukoteka.di.modules

import toothpick.InjectConstructor
import toothpick.config.Module
import javax.inject.Singleton

class RegistrationModule : Module() {

    init {
        bind(SimpleRegistration::class.java).toInstance(SimpleRegistration())
    }

}

@Singleton
@InjectConstructor
class SimpleRegistration constructor() {
    var login: String? = null
    var password: String? = null
}