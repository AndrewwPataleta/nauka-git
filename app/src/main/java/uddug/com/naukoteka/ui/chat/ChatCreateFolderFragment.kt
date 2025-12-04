package uddug.com.naukoteka.ui.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import java.util.ArrayList
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import uddug.com.naukoteka.R
import uddug.com.naukoteka.mvvm.chat.ChatCreateFolderEvent
import uddug.com.naukoteka.mvvm.chat.ChatCreateFolderViewModel
import uddug.com.naukoteka.mvvm.chat.ChatFolderSelectionItem
import uddug.com.naukoteka.ui.chat.compose.ChatCreateFolderScreen
import uddug.com.naukoteka.ui.theme.NaukotekaTheme

@AndroidEntryPoint
class ChatCreateFolderFragment : Fragment() {

    private val viewModel: ChatCreateFolderViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                NaukotekaTheme {
                    ChatCreateFolderScreen(
                        viewModel = viewModel,
                        onBackPressed = { findNavController().popBackStack() },
                        onAddChatsClick = { openAddChatsScreen() }
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeEvents()
        observeSelectedChatsResult()
    }

    private fun observeEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.events.collectLatest { event ->
                when (event) {
                    is ChatCreateFolderEvent.FolderCreated -> {
                        findNavController().previousBackStackEntry?.savedStateHandle?.set(
                            REFRESH_FOLDERS_KEY,
                            true
                        )
                        findNavController().popBackStack()
                    }

                    ChatCreateFolderEvent.ShowNameRequired -> {
                        Toast.makeText(
                            requireContext(),
                            R.string.chat_create_folder_name_required,
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    is ChatCreateFolderEvent.ShowError -> {
                        val message = event.message ?: getString(R.string.chat_create_folder_generic_error)
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun observeSelectedChatsResult() {
        findNavController().currentBackStackEntry?.savedStateHandle
            ?.getLiveData<ArrayList<ChatFolderSelectionItem>>(ChatFolderAddChatsFragment.SELECTED_CHATS_KEY)
            ?.observe(viewLifecycleOwner) { chats ->
                if (chats != null) {
                    viewModel.onChatsSelected(chats)
                    findNavController().currentBackStackEntry?.savedStateHandle
                        ?.set(ChatFolderAddChatsFragment.SELECTED_CHATS_KEY, null)
                }
            }
    }

    private fun openAddChatsScreen() {
        val selectedIds = viewModel.uiState.value.selectedChats
            .map { it.dialogId }
            .toLongArray()
        val args = Bundle().apply {
            putLongArray(ChatFolderAddChatsFragment.SELECTED_IDS_ARG, selectedIds)
        }
        findNavController().navigate(R.id.chatFolderAddChatsFragment, args)
    }

    companion object {
        const val REFRESH_FOLDERS_KEY = "refreshFolders"
    }
}
