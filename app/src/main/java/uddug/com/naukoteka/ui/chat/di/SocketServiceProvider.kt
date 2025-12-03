package uddug.com.naukoteka.ui.chat.di

import javax.inject.Inject
import javax.inject.Provider
import uddug.com.data.cache.cookies.CookiesCache

class SocketServiceProvider @Inject constructor(
    private val cookiesCache: CookiesCache,
) : Provider<SocketService> {
    override fun get(): SocketService = SocketServiceImpl(cookiesCache)
}
