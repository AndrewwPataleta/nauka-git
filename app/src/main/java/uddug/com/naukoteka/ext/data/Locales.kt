package uddug.com.naukoteka.ext.data

import uddug.com.naukoteka.R
import uddug.com.naukoteka.ui.fragments.login.CustomLanguage
import java.util.*

val RU_LOCALE = Locale("ru", "RU")
val EN_LOCALE = Locale("en", "US")
val AR_LOCALE = Locale("ar")

val CURRENTLY_ADDED_LANGUAGES = listOf(RU_LOCALE, EN_LOCALE, AR_LOCALE)

val SUPPORTED_LOCALES: MutableList<Locale> = mutableListOf(
    RU_LOCALE,
    EN_LOCALE,
    AR_LOCALE
)

val SUPPORTED_LOCALES_CUSTOM = listOf(
    CustomLanguage(R.string.language_english, EN_LOCALE),
    CustomLanguage(R.string.language_russian, RU_LOCALE),
    CustomLanguage(R.string.language_arabic, AR_LOCALE),
)