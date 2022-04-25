package paolo.udacity.downloader.platform.custom_views

import androidx.databinding.BindingAdapter
import paolo.udacity.core.dto.Event


@BindingAdapter("app:downloadProgress")
fun updateProgress(view: DownloadButtonView, progress: Double) {
    view.liveProgress.value = progress
}

@BindingAdapter("app:downloadResult")
fun updateProgress(view: DownloadButtonView, result: Event<Boolean>?) {
    view.liveResult.value = result?.peekContent()
}

@BindingAdapter("app:ready")
fun checkReadiness(view: DownloadButtonView, isReady: Boolean) {
    view.isReady = isReady
}