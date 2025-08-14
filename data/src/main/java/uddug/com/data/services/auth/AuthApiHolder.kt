package uddug.com.data.services.auth

import uddug.com.data.services.AuthApiService
import toothpick.InjectConstructor

@InjectConstructor
class AuthApiHolder {
    var authApi: AuthApiService? = null
}
