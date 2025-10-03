package uddug.com.domain.entities



data class Optional<T>(val value: T? = null)
fun <T> T?.asOptional() = Optional(this)
fun Optional<*>.isNull() = this.value == null


data class PrimitiveWrapper<out T>(val value: T)
