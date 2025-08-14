package uddug.com.data.services.models.response.user_profile

import com.google.gson.annotations.SerializedName


data class LogoDto (

  @SerializedName("id"          ) var id          : String? = null,
  @SerializedName("path"        ) var path        : String? = null,
  @SerializedName("fileName"    ) var fileName    : String? = null,
  @SerializedName("contentType" ) var contentType : String? = null,
  @SerializedName("fileSize"    ) var fileSize    : Int?    = null,
  @SerializedName("fileKind"    ) var fileKind    : Int?    = null,
  @SerializedName("fileType"    ) var fileType    : Int?    = null

)
