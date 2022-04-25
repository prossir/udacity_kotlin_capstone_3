package paolo.udacity.core.dto

import android.app.Activity
import android.content.Context
import android.os.Bundle
import androidx.annotation.NavigationRes


data class PendingNotificationInfo(
    val objectiveActivity: Activity,
    @NavigationRes val navigationGraph: Int? = null,
    val destination: Int? = null,
    val arguments: Bundle? = null
) {

    val context: Context
        get() = objectiveActivity.applicationContext

}