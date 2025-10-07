package uddug.com.data

import uddug.com.data.cache.cookies.CookiesCache
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import toothpick.InjectConstructor

@InjectConstructor
class NaukotekaCookieJar(private val cookiesCache: CookiesCache) : CookieJar {

    private var cookies: List<Cookie>? = null

    companion object {
        private val NKTS_COOKIE = "_nkts="
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val storedCookies = cookies?.filter { it.value.isNotEmpty() }
        val cachedCookie = cookiesCache.getAuthCookies()

        if (!storedCookies.isNullOrEmpty() && cachedCookie.isNotBlank()) {
            return storedCookies
        }

        if (cachedCookie.isBlank()) {
            cookies = null
            return emptyList()
        }

        return Cookie.parse(url, "${NKTS_COOKIE}$cachedCookie")
            ?.let { parsedCookie ->
                cookies = listOf(parsedCookie)
                cookies!!
            }
            ?: emptyList()
    }

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val nonEmptyCookies = cookies.filter { it.value.isNotEmpty() }
        this.cookies = if (nonEmptyCookies.isEmpty()) null else nonEmptyCookies

        val authCookieValue = nonEmptyCookies.firstOrNull()?.value
        if (authCookieValue != null) {
            cookiesCache.entity = authCookieValue
        } else {
            cookiesCache.clear()
        }
    }
}