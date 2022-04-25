package paolo.udacity.core.extensions

import android.app.DownloadManager
import android.database.Cursor


val Cursor.status: Int
    get() = this.intValueOfColumn(DownloadManager.COLUMN_STATUS)

fun Cursor.column(which: String) = this.getColumnIndex(which)
fun Cursor.intValueOfColumn(which: String): Int = this.getInt(column(which))
fun Cursor.isSuccessful() = this.status == DownloadManager.STATUS_SUCCESSFUL
fun Cursor.isSuccessfulOrHasFailed() = this.status == DownloadManager.STATUS_SUCCESSFUL
        || this.status == DownloadManager.STATUS_FAILED