package paolo.udacity.downloader.platform.view.common.views

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Bundle
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
import paolo.udacity.core.dto.Event
import paolo.udacity.core.extensions.*
import paolo.udacity.downloader.R
import paolo.udacity.downloader.platform.view.common.enums.DownloadEnum
import paolo.udacity.downloader.platform.view.common.utils.DownloaderConstants
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject


@HiltViewModel
class DownloaderViewModel(
    private val dispatcher: CoroutineDispatcher
): ViewModel() {

    @Inject constructor(): this(Dispatchers.IO)

    /**
     * @property _actionState, observable property  to display the errors during the download
     * process. */
    private val _actionState = MutableLiveData<DownloaderActionState>()
    val actionState: MutableLiveData<DownloaderActionState>
        get() = _actionState

    /**
     * @property _navigateToListDownloads, observable property for navigation back from the detail
     * fragment to the list fragment. Can only happen once. */
    private val _navigateToListDownloads = MutableLiveData<Event<String>>()
    val navigateToListDownloads : LiveData<Event<String>>
        get() = _navigateToListDownloads

    // Url written by the user
    /**
     * Used only when the user wants to manually write the url to download
     * @property _isOtherUrlSelected, the other url option is selected and is checked in the
     * internal functions of the viewModel. */
    private val _isOtherUrlSelected: MutableLiveData<Boolean> = MutableLiveData(false)
    val isOtherUrlSelected: LiveData<Boolean>
        get() = _isOtherUrlSelected
    /**
     * Used only when the user wants to manually write the url to download
     * @property _otherUrlToDownload, the custom URL that needs to be checked before
     * being downloaded. */
    private val _otherUrlToDownload: MutableLiveData<String> = MutableLiveData("")
    var otherUrlToDownload: MutableLiveData<String>
        get() = _otherUrlToDownload
        set(value) { _otherUrlToDownload.value = value.toString() }
     /**
      * @property otherUrlError
     */
    val otherUrlError: MutableLiveData<String?> = MutableLiveData()


    // Common variables
    /**
     * @property downloadObjective, the objective URL to be download. 3 fixed in an enum and 1
     * mutable. */
    private var downloadObjective: DownloadEnum? = null
    /**
     * @property readyForDownload, flag that checks whether a [downloadObjective] has been chosen
     * to be downloaded. Allows the animations on the download button to continue. */
    val readyForDownload: MutableLiveData<Boolean> = MutableLiveData(false)
    /**
     * @property downloadObjectiveName, accesor that gives the returns the name of the
     * [downloadObjective] if one has been chosen. Else it returns Uknown as a name. */
    val downloadObjectiveName : String
        get() = downloadObjective?.name ?: "Unknown"

    /**
     * @property downloadProgress, Determines the  progress of the download as it happens or an
     * approximated download rate if the download size is too small and does not update correctly. */
    val downloadProgress: MutableLiveData<Double> = MutableLiveData()
    /**
     * @property downloadResult, holds the result of the download downloaded by the DownloadManager
     * as a single use event. If true the [downloadObjective] was successfully downloaded;
     * if false it failed to download the [downloadObjective]. */
    val downloadResult: MutableLiveData<Event<Boolean>> = MutableLiveData()
    /**
     * @property downloadResultDescription, the download result description for the detailed
     * result. */
    val downloadResultDescription: String
        get() = if(downloadResult.value?.peekContent() == true) "Success" else "Failed"
    /**
     * @property downloadResultBundle, the download result */
    val downloadResultBundle: Bundle
        get() {
            return Bundle().apply {
                this.putBoolean(DownloaderConstants.DOWNLOADER_ARGUMENT_DOWNLOAD_RESULT,
                    downloadResult.value?.peekContent() == true)
                this.putSerializable(DownloaderConstants.DOWNLOADER_ARGUMENT_DOWNLOAD_OBJECTIVE,
                    downloadObjective!!)
            }
        }

    /**
     * As the DownloadManager does not always know the COLUMN_TOTAL_SIZE_BYTES due to it being
     * too small we add a small corrective projection related to an overall progress */
    private var smallDownloadProgressCorrection = 0.0

    /**
     * Sets a download objective for the DownloadManager to download. If a custom download objective
     * is set, then it allows the user to set the url to download the information from.
     * @param downloadObjective an enumeration that restricts the type of element that can be
     * downloaded. */
    fun setDownloadObjective(downloadObjective: DownloadEnum) {
        if(downloadObjective == DownloadEnum.OTHER) {
            _isOtherUrlSelected.value = true
            readyForDownload.postValue(false)
        }
        else {
            _isOtherUrlSelected.value = false
            readyForDownload.postValue(true)
        }
        this.downloadObjective = downloadObjective
    }

    /**
     * It checks
     * @param context: A context to use for downloading.
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
            if(_otherUrlToDownload.value.isNullOrBlank()) {
                return "When selecting others option, you must specify an url."
            }
        }
        return null
    }

    fun checkOtherUrlValidity(urlStringCandidate: String) {
        if(urlStringCandidate.isValidUrl) {
            downloadObjective?.setUrl(urlStringCandidate)
            otherUrlError.postValue(null)
            readyForDownload.postValue(true)
        }
        else {
            otherUrlError.postValue("Invalid URL")
        }
    }

    /**
     * Downloads a file using DownLoadManager an string origin related to a request
     * */
    private fun download(context: Context) {
        viewModelScope.safeLaunch(::onDownloadError) {
            with(dispatcher) {
                val request =
                    DownloadManager.Request(Uri.parse(downloadObjective!!.getUrl()))
                        .setTitle(context.getString(paolo.udacity.core.R.string.app_name))
                        .setDescription(context.getString(R.string.download_manager_app_description))
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
                    smallDownloadProgressCorrection = 0.0
                    downloadResult.postValue(Event(cursor.isSuccessful()))
                }
                else -> {
                    smallDownloadProgressCorrection += 0.5
                    emit(calculateDownloadProgressAsPercentage(bytesDownloaded, bytesTotal))
                }
            }
            cursor.close()

            if (isDownloading.get()) delay(50)
        }
    }.flowOn(Dispatchers.IO)

    private fun calculateDownloadProgressAsPercentage(bytesDownloaded: Int, bytesTotal: Int) : Double {
        return if(bytesTotal <= 0) {
            if(smallDownloadProgressCorrection >= 100.0) 98.0 else smallDownloadProgressCorrection
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