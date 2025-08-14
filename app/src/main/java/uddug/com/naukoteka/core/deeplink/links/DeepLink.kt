package uddug.com.naukoteka.core.deeplink.links


import uddug.com.naukoteka.core.deeplink.DoNotObfuscate

sealed class DeepLink(val scheme: Scheme) : DoNotObfuscate {

}

enum class Scheme(val module: String) {
    CHAT(module = "chat"),
    PROFILE(module = "profile"),
}