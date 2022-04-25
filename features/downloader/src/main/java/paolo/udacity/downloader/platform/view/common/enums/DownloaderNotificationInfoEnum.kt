package paolo.udacity.downloader.platform.view.common.enums

import paolo.udacity.core.dto.NotificationInfo
import paolo.udacity.downloader.R


enum class DownloaderNotificationInfoEnum(val notificationInfo: NotificationInfo) {
    DEFAULT(NotificationInfo(channelId = R.string.downloader_notification_channel_id,
        icon = R.drawable.ic_download_notification_icon,
        title = "Download finished",
        messageBody = "The download has finished. To see the result please tap inside this notification."
    ));
}