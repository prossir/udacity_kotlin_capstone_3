package paolo.udacity.downloader.platform.view.list_downloadables.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import paolo.udacity.core.utils.InternalNotificationUtil
import paolo.udacity.downloader.R
import paolo.udacity.downloader.databinding.FragmentListDownloadsBinding
import paolo.udacity.downloader.platform.view.common.enums.DownloaderNotificationInfoEnum
import paolo.udacity.downloader.platform.view.common.views.DownloaderActionState
import paolo.udacity.downloader.platform.view.common.views.DownloaderViewModel
import timber.log.Timber


class ListDownloadsFragment : Fragment() {

    private lateinit var binding : FragmentListDownloadsBinding
    private val viewModel: DownloaderViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentListDownloadsBinding.inflate(inflater)
        initObservers()
        initUi()
        return binding.root
    }

    private fun initObservers() {
        viewModel.actionState.observe(viewLifecycleOwner) { state ->
            when(state) {
                is DownloaderActionState.OnError -> {
                    Toast.makeText(requireContext(), state.errors, Toast.LENGTH_SHORT).show()
                }
                else -> {
                    Timber.i("Impossible state: ListDownloadsFragment.viewState.")
                }
            }
        }

        viewModel.downloadResult.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let {
                InternalNotificationUtil.sendNotification(activity = requireActivity(),
                    navigationGraphId = R.navigation.downloader_nav_graph,
                    destinationId = R.id.downloadedDetailFragment,
                    bundle = viewModel.downloadResultBundle,
                    notificationInfo = DownloaderNotificationInfoEnum.DEFAULT.notificationInfo
                )
            }
        }
    }

    private fun initUi() {
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
    }

}