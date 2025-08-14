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
        println("try to get cook")
        if (cookies != null) {
            return cookies?.filter { it.value.isNotEmpty() }!!
        } else {
            return listOf(
                Cookie.parse(url, "${NKTS_COOKIE}${cookiesCache.getAuthCookies()}")!!
            )
        }
    }

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        this.cookies = cookies
        cookiesCache.entity = cookies.find { it.value.isNotEmpty() }?.value.toString()
    }
}