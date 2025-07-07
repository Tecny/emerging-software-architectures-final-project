import android.util.Log
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

class MyCookieJar : CookieJar {
    private val cookieStore: MutableMap<String, MutableList<Cookie>> = mutableMapOf()

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val host = url.host
        if (!cookieStore.containsKey(host)) {
            cookieStore[host] = mutableListOf()
        }

        val existingCookies = cookieStore[host]!!
        for (cookie in cookies) {
            if (cookie.name != "ARRAffinity" && cookie.name != "ARRAffinitySameSite") {
                existingCookies.removeAll { it.name == cookie.name }
                existingCookies.add(cookie)
                Log.d("COOKIE", "Saving cookie: ${cookie.name} = ${cookie.value}")
            }
        }
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val cookies = cookieStore[url.host]?.filter {
            it.name != "ARRAffinity" && it.name != "ARRAffinitySameSite"
        } ?: emptyList()

        cookies.forEach { cookie ->
            Log.d("COOKIE", "Sending cookie: ${cookie.name}=${cookie.value}")
        }
        return cookies
    }
}