package paolo.udacity.core.extensions

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.core.app.NotificationCompat
import androidx.navigation.NavDeepLinkBuilder
import paolo.udacity.core.dto.NotificationInfo
import paolo.udacity.core.dto.PendingNotificationInfo


/**
 * Builds and delivers the notification.
 * @param pendingNotificationInfo, activity context.
 * @param notificationInfo, .
 */
fun NotificationManager.sendNotification(pendingNotificationInfo: PendingNotificationInfo,
                                         notificationInfo: NotificationInfo) {
    // Create the content intent for the notification, which launches
    val contentPendingIntent: PendingIntent
    if(pendingNotificationInfo.navigationGraph != null) {
        contentPendingIntent = NavDeepLinkBuilder(pendingNotificationInfo.context)
            .setComponentName(pendingNotificationInfo.objectiveActivity::class.java)
            .setGraph(pendingNotificationInfo.navigationGraph)
            .setDestination(pendingNotificationInfo.destination!!)
            .setArguments(pendingNotificationInfo.arguments)
            .createPendingIntent()

    } else {
        val contentIntent = Intent(pendingNotificationInfo.context,
            pendingNotificationInfo.objectiveActivity::class.java)
        contentPendingIntent = PendingIntent.getActivity(
            pendingNotificationInfo.context,
            NOTIFICATION_ID,
            contentIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    val iconAsBitmap = BitmapFactory.decodeResource(
        pendingNotificationInfo.context.resources,
        notificationInfo.icon
    )
    val bigPicStyle = NotificationCompat.BigPictureStyle()
        .bigPicture(iconAsBitmap)
        .bigLargeIcon(null)

    // Build the notification
    val builder = NotificationCompat.Builder(
        pendingNotificationInfo.context,
        pendingNotificationInfo.context.getString(notificationInfo.channelId)
    )
        .setSmallIcon(notificationInfo.icon)
        .setContentTitle(notificationInfo.title)
        .setContentText(notificationInfo.messageBody)
        .setContentIntent(contentPendingIntent)
        //Also set setAutoCancel() to true, so that when the user taps on the notification,
        // the notification dismisses itself as it takes you to the app.
        .setAutoCancel(true)
        .setStyle(bigPicStyle)
        .setLargeIcon(iconAsBitmap)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
    notify(NOTIFICATION_ID, builder.build())
}

/**
 * Cancels all notifications.
 */
fun NotificationManager.cancelNotifications() {
    cancelAll()
}

// Notification ID.
private const val NOTIFICATION_ID = 0
