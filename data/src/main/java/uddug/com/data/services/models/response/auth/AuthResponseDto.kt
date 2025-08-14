package uddug.com.data.services.models.response.auth

import com.google.gson.annotations.SerializedName


data class AuthResponseDto(
    @SerializedName("userStatus")
    val userStatus: UserStatusDto,
    @SerializedName("error")
    val error: ErrorDto? = null,
    @SerializedName("validationErrors")
    val validationErrors: List<ValidationErrorDto>? = null
)

data class UserStatusDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("enabled")
    val enabled: Boolean,
    @SerializedName("userActions")
    val userActions: UserActionsDto?
)

data class UserActionsDto(
    @SerializedName("actions")
    val actions: List<ActionDto>
)

data class ActionDto(
    @SerializedName("name")
    val name: String,
    @SerializedName("uri")
    val uri: Any? = null
)

data class ErrorDto(
    @SerializedName("code")
    val code: Int,
    @SerializedName("textCode")
    val textCode: String,
    @SerializedName("type")
    val type: String,
    @SerializedName("message")
    val message: String
)

data class  ValidationErrorDto(
    @SerializedName("field")
    val `field`: String,
    @SerializedName("message")
    val message: String
)
