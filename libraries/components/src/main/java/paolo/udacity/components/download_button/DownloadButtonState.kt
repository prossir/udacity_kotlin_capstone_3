package paolo.udacity.components.download_button

import androidx.annotation.ColorInt
import androidx.annotation.StringRes
import paolo.udacity.core.R as colorOrigin
import paolo.udacity.components.R as stringOrigin


enum class DownloadButtonState(@ColorInt var buttonColor: Int,
                               @ColorInt var textColor: Int,
                               @StringRes var buttonText: Int) {
    // When IDLE the color does not show
    IDLE(colorOrigin.color.purple_500, colorOrigin.color.white,
        stringOrigin.string.download_button_idle_text),
    DOWNLOADING(colorOrigin.color.purple_500, colorOrigin.color.white,
        stringOrigin.string.download_button_downloading_text),
    SUCCESSFUL(colorOrigin.color.teal_200, colorOrigin.color.black,
        stringOrigin.string.download_button_successful_text),
    ERROR(colorOrigin.color.red, colorOrigin.color.black,
        stringOrigin.string.download_button_error_text);

    fun next(wasSuccessful: Boolean = true) = when (this) {
        IDLE -> DOWNLOADING
        DOWNLOADING -> if(wasSuccessful) SUCCESSFUL else ERROR
        SUCCESSFUL -> IDLE
        ERROR -> IDLE
    }

}