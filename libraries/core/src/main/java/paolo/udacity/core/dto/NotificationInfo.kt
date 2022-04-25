package paolo.udacity.core.dto

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes


/**
 * Builds and delivers the notification.
 * @param channelId, activity context.
 * @param icon, activity context.
 * @param title, .
 * @param messageBody, .  */
data class NotificationInfo(
    @StringRes val channelId: Int,
    @DrawableRes val icon: Int,
    val title: String,
    val messageBody: String
)