package uddug.com.domain.entities

// todo refactor mb
// We need it to emit optionals in RxJava
data class Optional<T>(val value: T? = null)
fun <T> T?.asOptional() = Optional(this)
fun Optional<*>.isNull() = this.value == null

// see: https://youtrack.jetbrains.com/issue/KT-18918
data class PrimitiveWrapper<out T>(val value: T)