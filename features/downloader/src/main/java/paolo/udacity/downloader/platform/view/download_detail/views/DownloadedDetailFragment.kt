package paolo.udacity.downloader.platform.view.download_detail.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import paolo.udacity.core.dto.Event
import paolo.udacity.downloader.databinding.FragmentDownloadDetailBinding
import paolo.udacity.downloader.platform.view.common.views.DownloaderViewModel


class DownloadedDetailFragment : Fragment() {

    private lateinit var binding : FragmentDownloadDetailBinding
    private val viewModel: DownloaderViewModel by activityViewModels()
    private val arguments: DownloadedDetailFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentDownloadDetailBinding.inflate(inflater)
        initArguments()
        initObservers()
        initUi()
        return binding.root
    }

    private fun initArguments() {
        arguments.downloadResult.let {
            viewModel.downloadResult.value = Event(it, true)
        }

        arguments.downloadObjective.let {
            viewModel.setDownloadObjective(it)
        }
    }

    private fun initObservers() {
        viewModel.navigateToListDownloads.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let {
                view?.findNavController()?.popBackStack()
            }
        }
    }

    private fun initUi() {
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
    }

}