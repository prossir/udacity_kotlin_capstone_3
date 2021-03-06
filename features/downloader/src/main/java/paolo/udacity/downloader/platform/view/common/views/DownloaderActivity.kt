package paolo.udacity.downloader.platform.view.common.views

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import dagger.hilt.android.AndroidEntryPoint
import paolo.udacity.core.utils.InternalNotificationUtil
import paolo.udacity.downloader.R
import paolo.udacity.downloader.databinding.ActivityDownloaderBinding


@AndroidEntryPoint
class DownloaderActivity: AppCompatActivity(), MotionLayout.TransitionListener {

    private lateinit var binding: ActivityDownloaderBinding
    private val viewModel: DownloaderViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_downloader)
        initUi()
        initNotifications()
    }

    private fun initUi() {
        setSupportActionBar(binding.toolbar)
        NavigationUI.setupActionBarWithNavController(this, binding.navHostFragment.getFragment<NavHostFragment>().navController)

        (binding.root as MotionLayout).addTransitionListener(this)
    }

    private fun initNotifications() {
        InternalNotificationUtil.createChannel(activity = this,
            channelId = getString(R.string.downloader_notification_channel_id),
            channelName = getString(R.string.downloader_notification_channel_name),
            channelDescription = getString(R.string.downloader_notification_channel_description)
        )
    }

    override fun onSupportNavigateUp(): Boolean {
        return findNavController(R.id.nav_host_fragment).navigateUp()
    }

    /* MotionLayout.TransitionListener implementations */
    override fun onTransitionStarted(motionLayout: MotionLayout?, startId: Int, endId: Int) {}

    override fun onTransitionChange(motionLayout: MotionLayout?, startId: Int, endId: Int,
                                    progress: Float) {}

    override fun onTransitionCompleted(motionLayout: MotionLayout?, currentId: Int) {
        viewModel.dismissDownloadDetail()
        motionLayout?.transitionToStart()
    }

    override fun onTransitionTrigger(motionLayout: MotionLayout?, triggerId: Int, positive: Boolean,
        progress: Float) {}

}