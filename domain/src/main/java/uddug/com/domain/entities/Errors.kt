package uddug.com.domain.entities

import com.google.gson.annotations.SerializedName



















enum class ErrorCode(val code: Int) {
    @SerializedName("0")
    Ok(0),

    @SerializedName("1")
    UserWithEmailNotFound(1),

    @SerializedName("2")
    UserWithPhoneNotFound(2),

    @SerializedName("3")
    IncorrectPassword(3),

    @SerializedName("4")
    UserWithEmailAlreadyExist(4),

    @SerializedName("5")
    UserWithNumberAlreadyExist(5),

    @SerializedName("6")
    SendingSmsTemporaryNotAvailable(6),

    @SerializedName("7")
    SendingLetterTemporaryNotAvailable(7)


}
