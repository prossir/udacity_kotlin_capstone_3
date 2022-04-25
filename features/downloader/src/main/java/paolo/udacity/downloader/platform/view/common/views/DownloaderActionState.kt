package paolo.udacity.downloader.platform.view.common.views


/**
 * For events that should only repeat once
 * */
sealed class DownloaderActionState {

    data class OnError(val errors: String): DownloaderActionState()

}