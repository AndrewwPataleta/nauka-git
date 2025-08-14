package com.nauchat.core.deeplinkngenerator.links

data class DeepLinkProfile(
    val profileId: String? = null
) : DeepLink(scheme = Scheme.PROFILE)

data class DeepLinkProfileInfo(
    val profileId: String? = null
) : DeepLink(scheme = Scheme.PROFILE_INFO)

enum class ProfileDeeplinkValues {
    PROFILE_ID
}
