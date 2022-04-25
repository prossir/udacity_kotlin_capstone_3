package paolo.udacity.downloader.platform.custom_views

import androidx.annotation.ColorInt
import androidx.annotation.StringRes
import paolo.udacity.downloader.R


enum class DownloadButtonState(@ColorInt var buttonColor: Int,
                               @ColorInt var textColor: Int,
                               @StringRes var buttonText: Int) {
    // When IDLE the color does not show
    IDLE(paolo.udacity.core.R.color.purple_500, paolo.udacity.core.R.color.white,
        R.string.download_button_idle_text),
    DOWNLOADING(paolo.udacity.core.R.color.purple_500, paolo.udacity.core.R.color.white,
        R.string.download_button_downloading_text),
    SUCCESSFUL(paolo.udacity.core.R.color.teal_200, paolo.udacity.core.R.color.black,
        R.string.download_button_successful_text),
    ERROR(paolo.udacity.core.R.color.red, paolo.udacity.core.R.color.black,
        R.string.download_button_error_text);

    fun next(wasSuccessful: Boolean = true) = when (this) {
        IDLE -> DOWNLOADING
        DOWNLOADING -> if(wasSuccessful) SUCCESSFUL else ERROR
        SUCCESSFUL -> IDLE
        ERROR -> IDLE
    }

}