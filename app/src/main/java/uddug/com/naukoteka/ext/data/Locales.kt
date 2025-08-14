package uddug.com.naukoteka.ext.data

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
    CustomLanguage("English", EN_LOCALE),
    CustomLanguage("Русский", RU_LOCALE),
    CustomLanguage("عرب", AR_LOCALE),
)