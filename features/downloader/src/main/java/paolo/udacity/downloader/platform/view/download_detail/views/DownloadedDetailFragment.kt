package paolo.udacity.downloader.platform.view.download_detail.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import paolo.udacity.downloader.databinding.FragmentDownloadDetailBinding
import paolo.udacity.downloader.platform.view.common.views.DownloaderViewModel


class DownloadedDetailFragment : Fragment() {

    private lateinit var binding : FragmentDownloadDetailBinding
    private val viewModel: DownloaderViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentDownloadDetailBinding.inflate(inflater)
        initObservers()
        initUi()
        return binding.root
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