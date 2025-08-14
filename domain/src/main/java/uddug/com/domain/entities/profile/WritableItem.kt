package uddug.com.domain.entities.profile

import com.google.gson.annotations.SerializedName


data class WritableItem(

    @SerializedName("id") var id: String? = null,
    @SerializedName("sd") var sd: String? = null,
    @SerializedName("ed") var ed: String? = null,
    @SerializedName("cType") var cType: String? = null,
    @SerializedName("body") var body: String? = null,
    @SerializedName("title") var title: String? = null,
    @SerializedName("subtitle") var subtitle: String? = null,
    @SerializedName("dsc") var dsc: String? = null,
    @SerializedName("link") var link: String? = null,
    @SerializedName("cLang") var cLang: String? = null,
    @SerializedName("ttl") var ttl: String? = null,
    @SerializedName("skipHours") var skipHours: String? = null,
    @SerializedName("skipDays") var skipDays: String? = null,
    @SerializedName("pubDate") var pubDate: String? = null,
    @SerializedName("lastBuildDate") var lastBuildDate: String? = null,
    @SerializedName("cFeedSource") var cFeedSource: String? = null,
    @SerializedName("rOwner") var rOwner: String? = null,
    @SerializedName("image") var image: String? = null,
    @SerializedName("uref") var uref: String? = null

)