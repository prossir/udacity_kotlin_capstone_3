package paolo.udacity.downloader.platform.view.common.enums

import paolo.udacity.downloader.platform.view.common.model.DownloadModel
import java.lang.Exception


enum class DownloadEnum(private val download: DownloadModel) {

    GLIDE(DownloadModel(1, "Glide", "https://github.com/bumptech/glide")),
    LOAD_APP(
        DownloadModel(2, "LoadApp",
        "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter")
    ),
    RETROFIT(DownloadModel(3, "Retrofit", "https://github.com/square/retrofit")),
    OTHER(DownloadModel(4, "Other", ""));

    fun getUrl(): String = this.download.url

    fun setUrl(url: String) {
        if(this == OTHER) {
            this.download.url = url
        } else {
            throw Exception("The operation can only be applied to the OTHER DownloadEnum option.")
        }
    }

}