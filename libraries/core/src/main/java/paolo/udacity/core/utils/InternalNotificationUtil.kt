package paolo.udacity.core.utils

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.core.content.ContextCompat
import paolo.udacity.core.dto.NotificationInfo
import paolo.udacity.core.dto.PendingNotificationInfo
import paolo.udacity.core.extensions.sendNotification


object InternalNotificationUtil {

    fun createChannel(activity: Activity, channelId: String, channelName: String,
                              channelDescription: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                setShowBadge(false)
            }
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.enableVibration(true)
            notificationChannel.description = channelDescription

            val notificationManager = activity.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    fun sendNotification(activity: Activity, navigationGraphId: Int,
                         destinationId: Int, bundle: Bundle?, notificationInfo: NotificationInfo) {
        val notificationManager = ContextCompat.getSystemService(activity,
            NotificationManager::class.java) as NotificationManager
        val pendingNotificationInfo = PendingNotificationInfo(activity, navigationGraphId, destinationId, bundle)
        notificationManager.sendNotification(pendingNotificationInfo, notificationInfo)
    }

    /*fun sendNotification(activity: Activity, notificationInfo: NotificationInfo) {
        val notificationManager = ContextCompat.getSystemService(activity,
        NotificationManager::class.java) as NotificationManager
        val pendingNotificationInfo = PendingNotificationInfo(activity)
        notificationManager.sendNotification(pendingNotificationInfo, notificationInfo)
    } */

}