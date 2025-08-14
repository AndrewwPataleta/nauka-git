package uddug.com.data.validator

import android.util.Patterns
import uddug.com.domain.validator.EmailValidator
import uddug.com.domain.validator.PhoneValidator
import toothpick.InjectConstructor

@InjectConstructor
class FieldsValidator : EmailValidator, PhoneValidator {

    override fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    override fun isValidPhone(phone: String): Boolean {
        val RU_NUMBER_VALID_COUNT = 11
        return Patterns.PHONE.matcher(phone).matches() && phone.count() == RU_NUMBER_VALID_COUNT
    }

    fun isNotEmpty(field: String): Boolean = field.isNotEmpty()
    fun isEquals(password: String, passwordConfirmation: String) = password == passwordConfirmation

}