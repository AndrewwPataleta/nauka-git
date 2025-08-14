package uddug.com.naukoteka.utils

fun <T> T.getOrNullCondition(condition: Boolean): T? {
    return if (condition) this else null
}