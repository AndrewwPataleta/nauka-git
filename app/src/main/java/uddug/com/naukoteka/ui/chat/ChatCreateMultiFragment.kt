package uddug.com.naukoteka.ui.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import uddug.com.naukoteka.mvvm.chat.ChatCreateMultiEvent
import uddug.com.naukoteka.mvvm.chat.ChatCreateMultiViewModel
import uddug.com.naukoteka.mvvm.chat.ChatListViewModel
import uddug.com.naukoteka.ui.chat.compose.ChatCreateMultiScreen

@AndroidEntryPoint
class ChatCreateMultiFragment : Fragment() {

    private val viewModel: ChatCreateMultiViewModel by viewModels()
    private val chatListViewModel: ChatListViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        setupObservers()
        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    ChatCreateMultiScreen(
                        viewModel = viewModel,
                        onCreateClick = { viewModel.onCreateGroupClick() },
                        onBackPressed = { findNavController().popBackStack() },
                    )
                }
            }
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.events.collectLatest { event ->
                when (event) {
                    ChatCreateMultiEvent.CloseAndRefresh -> {
                        chatListViewModel.refreshChats()
                        findNavController().popBackStack()
                    }
                }
            }
        }
    }
}
