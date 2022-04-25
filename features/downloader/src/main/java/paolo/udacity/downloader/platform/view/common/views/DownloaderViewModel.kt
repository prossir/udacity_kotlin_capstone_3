package paolo.udacity.downloader.platform.view.common.views

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import paolo.udacity.core.R
import paolo.udacity.core.extensions.intValueOfColumn
import paolo.udacity.core.extensions.isSuccessful
import paolo.udacity.core.extensions.isSuccessfulOrHasFailed
import paolo.udacity.core.dto.Event
import paolo.udacity.core.extensions.safeLaunch
import paolo.udacity.core.extensions.with
import paolo.udacity.downloader.platform.view.common.enums.DownloadEnum
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject


@HiltViewModel
class DownloaderViewModel(
    private val dispatcher: CoroutineDispatcher
): ViewModel() {

    @Inject constructor(): this(Dispatchers.IO)

    private val _actionState = MutableLiveData<DownloaderActionState>()
    val actionState: MutableLiveData<DownloaderActionState>
        get() = _actionState

    private val _navigateToListDownloads = MutableLiveData<Event<String>>()
    val navigateToListDownloads : LiveData<Event<String>>
        get() = _navigateToListDownloads

    /**
     * Used only when the user wants to manually write the address to download what they want
     * @property _isOtherUrlSelected the other url option is selected and is checked in the
     * internal functions of the viewModel.
     */
    private var _isOtherUrlSelected: MutableLiveData<Boolean> = MutableLiveData(false)
    val isOtherUrlSelected: LiveData<Boolean>
        get() = _isOtherUrlSelected

    private var _manualUrlToDownload: MutableLiveData<String> = MutableLiveData("")
    var manualUrlToDownload: MutableLiveData<String>
        get() = _manualUrlToDownload
        set(value) { _manualUrlToDownload.value = value.toString() }

    /**
     * Used only when the user wants to manually write the address to download what they want
     */
    private var downloadObjective: DownloadEnum? = null
    val readyForDownload: MutableLiveData<Boolean> = MutableLiveData(false)
    val downloadName : String
        get() = downloadObjective?.name ?: "Unknown"

    /**
     * Determines the  progress of the download live
     */
    val downloadProgress: MutableLiveData<Double> = MutableLiveData()
    val downloadResult: MutableLiveData<Event<Boolean>> = MutableLiveData()
    val downloadResultDescription : String
        get() = if(downloadResult.value?.peekContent() == true) "Success" else "Failed"
    /**
     * As the DownloadManager does not always know the COLUMN_TOTAL_SIZE_BYTES due to it being
     * too small we add a small corrective projection related to an overall progress
     * */
    private var smallDownloadCorrection = 0.0

    /**
     * Sets a download objective for the DownloadManager to download. If a custom download objective
     * is set, then it allows the user to set the url to download the information from.
     * @param downloadObjective an enumeration that restricts the type of element that can be
     * downloaded
     */
    fun setDownloadObjective(downloadObjective: DownloadEnum) {
        _isOtherUrlSelected.value = downloadObjective == DownloadEnum.OTHER
        this.downloadObjective = downloadObjective
        readyForDownload.postValue(true)
    }

    /**
     * @param context: A context to use the download manager and access strings in the xml file
     */
    fun tryDownload(context: Context) {
        checkBeforeDownload()?.let { errors ->
            onCommonError(errors)
        } ?: kotlin.run {
            download(context)
        }
    }

    private fun onCommonError(errors: String) {
        _actionState.postValue(DownloaderActionState.OnError(errors))
    }

    /**
     *
     * */
    private fun checkBeforeDownload() : String? {
        if(downloadObjective == null) {
            return "You must choose an option from the list of downloadable options."
        }

        if(downloadObjective == DownloadEnum.OTHER) {
            if(_manualUrlToDownload.value.isNullOrBlank()) {
                return "When selecting others option, you must specify an url."
            }
        }
        return null
    }

    /**
     * Downloads a file using DownLoadManager an string origin related to a request
     * */
    private fun download(context: Context) {
        viewModelScope.safeLaunch(::onDownloadError) {
            with(dispatcher) {
                val request =
                    DownloadManager.Request(Uri.parse(downloadObjective!!.getUrl()))
                        .setTitle(context.getString(R.string.app_name))
                        .setDescription(context.getString(R.string.app_description))
                        .setRequiresCharging(false)
                        .setAllowedOverMetered(true)
                        .setAllowedOverRoaming(true)

                (context.getSystemService(AppCompatActivity.DOWNLOAD_SERVICE) as DownloadManager).apply {
                    this.enqueue(request).let { downloadId ->
                        observeDownloadStatus(downloadId, this).collect { progress ->
                            downloadProgress.postValue(progress)
                        }
                    }
                }
            }
        }
    }

    private fun observeDownloadStatus(downloadId: Long, downloadManager: DownloadManager) = flow {
        val isDownloading = AtomicBoolean(true)
        while (isDownloading.get()) {
            val query = DownloadManager.Query().setFilterById(downloadId)
            val cursor = downloadManager.query(query)
            cursor.moveToFirst()
            val bytesDownloaded = cursor!!.intValueOfColumn(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
            val bytesTotal = cursor.intValueOfColumn(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
            when {
                cursor.isSuccessfulOrHasFailed() -> {
                    isDownloading.set(false)
                    emit(100.0)
                    smallDownloadCorrection = 0.0
                    downloadResult.postValue(Event(cursor.isSuccessful()))
                }
                else -> {
                    smallDownloadCorrection += 0.5
                    emit(calculateDownloadProgressAsPercentage(bytesDownloaded, bytesTotal))
                }
            }
            cursor.close()

            if (isDownloading.get()) delay(50)
        }
    }.flowOn(Dispatchers.IO)

    private fun calculateDownloadProgressAsPercentage(bytesDownloaded: Int, bytesTotal: Int) : Double {
        return if(bytesTotal <= 0) {
            if(smallDownloadCorrection >= 100.0) 98.0 else smallDownloadCorrection
        } else {
            (bytesDownloaded * 1.0)/bytesTotal
        }
    }

    private fun onDownloadError(t: Throwable) {
        Timber.d(t)
        _actionState.postValue(DownloaderActionState.OnError("There was an error when downloading the module"))
    }

    fun dismissDownloadDetail() {
        _navigateToListDownloads.postValue(Event(System.currentTimeMillis().toString()))
    }

}