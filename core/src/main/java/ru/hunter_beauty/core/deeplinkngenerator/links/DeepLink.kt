package com.nauchat.core.deeplinkngenerator.links

sealed class DeepLink(val scheme: Scheme)

enum class Scheme(val module: String) {
    PROFILE(module = "profile"),
}
