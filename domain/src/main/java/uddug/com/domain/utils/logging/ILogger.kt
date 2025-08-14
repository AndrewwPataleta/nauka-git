package uddug.com.domain.utils.logging

interface ILogger {
    fun debug(message: String)
    fun report(message: String, reportInDebug: Boolean = false)
    fun info(message: String)
    fun warn(message: String, throwable: Throwable? = null)
    fun error(message: String, throwable: Throwable)

    fun setUserIdentifier(identifier: String)
    fun<T> setState(key: String, value: T)
}