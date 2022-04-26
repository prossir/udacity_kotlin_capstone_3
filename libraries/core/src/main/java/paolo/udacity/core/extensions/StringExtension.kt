package paolo.udacity.core.extensions

import android.util.Patterns
import android.webkit.URLUtil
import timber.log.Timber
import java.net.MalformedURLException
import java.net.URL


val String.isValidUrl: Boolean
    get() {
        try {
            val url = URL(this) // checks whether Url is malformed
            return URLUtil.isValidUrl(this) && Patterns.WEB_URL.matcher(this).matches()
        } catch (e: MalformedURLException) {
            Timber.d(e)
        }
        return false
    }
