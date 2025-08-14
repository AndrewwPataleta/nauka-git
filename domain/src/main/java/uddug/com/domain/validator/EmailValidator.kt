package uddug.com.domain.validator

interface EmailValidator {
    fun isValidEmail(email : String): Boolean
}