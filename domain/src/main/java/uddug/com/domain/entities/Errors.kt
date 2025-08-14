package uddug.com.domain.entities

import com.google.gson.annotations.SerializedName

/**
    code = 0, OK

    code = 1, пользователь с такой почтой не найден

    code = 2, пользователь с таким телефоном не найден

    code = 3, некорректный пароль

    code = 4, пользователь c такой почтой уже существует

    code = 5, пользователь c таким номером уже существует

    code = 6, отправка СМС временно запрещена

    code = 7, отправка письма временно запрещена
 */

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