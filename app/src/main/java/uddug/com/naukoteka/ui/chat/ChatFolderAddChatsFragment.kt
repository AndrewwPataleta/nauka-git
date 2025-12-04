package uddug.com.naukoteka.ui.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import uddug.com.naukoteka.mvvm.chat.ChatFolderAddChatsEvent
import uddug.com.naukoteka.mvvm.chat.ChatFolderAddChatsViewModel
import uddug.com.naukoteka.ui.chat.compose.ChatFolderAddChatsScreen
import uddug.com.naukoteka.ui.theme.NaukotekaTheme
import java.util.ArrayList

@AndroidEntryPoint
class ChatFolderAddChatsFragment : Fragment() {

    private val viewModel: ChatFolderAddChatsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                NaukotekaTheme {
                    ChatFolderAddChatsScreen(
                        viewModel = viewModel,
                        onBackPressed = { viewModel.onBackPressed() }
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeEvents()
    }

    private fun observeEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.events.collectLatest { event ->
                when (event) {
                    ChatFolderAddChatsEvent.Cancel -> {
                        findNavController().popBackStack()
                    }
                    is ChatFolderAddChatsEvent.ChatsApplied -> {
                        findNavController().previousBackStackEntry?.savedStateHandle?.set(
                            SELECTED_CHATS_KEY,
                            ArrayList(event.chats)
                        )
                        findNavController().popBackStack()
                    }
                }
            }
        }
    }

    companion object {
        const val SELECTED_IDS_ARG = "selected_ids"
        const val SELECTED_CHATS_KEY = "selected_chats"
    }
}
