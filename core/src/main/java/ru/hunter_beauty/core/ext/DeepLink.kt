package com.nauchat.core.ext

import com.google.gson.Gson

const val DEEP_LINK_SEPARATOR = "://"

private val gson: Gson by lazy {
    Gson()
}

fun generateDeepLink(
    protocol: String = DeepLinkProtocol.APP.protocol,
    module: Module,
    params: Map<String, Any> = emptyMap(),
): String {
    var deep = module.deep(protocol)
    doIfTrue(params.entries.isNotEmpty()) {
        deep = deep.plus("/?")
    }
    params.entries.forEachIndexed { index, mutableEntry ->
        deep = deep.plus(
            mutableEntry.key + "=" + mutableEntry.value
        )
        deep = deep.plus(",")
    }
    if(deep.last() == ',') deep = deep.dropLast(1)
    return deep
}

enum class Module(val moduleName: String) {
    ONBOARDING_WELCOME("onboarding_welcome"),
    ONBOARDING_GENDER("onboarding_gender"),
    ONBOARDING_BIO("onboarding_bio"),
    ONBOARDING_LOCATION("onboarding_location"),
    ONBOARDING_LOCATION_BY_HAND("onboarding_location_by_hand"),
    ONBOARDING_TO_APP("onboarding_to_app"),
    MAP_WELCOME("map_welcome"),
    SPECIALIST_WELCOME("specialist_welcome"),
    PROFILE_WELCOME("profile_welcome"),
    PROFILE_INFO_WELCOME("profile_info_welcome"),
    ALL_FAVORITES("all_favorites"),
    ORGANISATION_WELCOME("organisation_welcome"),
    DIALOGS_WELCOME("dialogs_welcome"),
    DIALOGS_DETAIL_CHAT("dialogs_detail_chat"),
    SPLASH("splash"),
    SERVICE_WELCOME("service_welcome"),
    APPOINTMENT_WELCOME("appointment_welcome"),
    RECORDS("records"),
    ALL_REVIEWS("all_reviews")
}

enum class DeepLinkProtocol(val protocol: String) {
    APP("app"),
    WEB("web")
}

fun Module.deep(protocol: String) = protocol + DEEP_LINK_SEPARATOR + this.moduleName
