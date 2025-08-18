package uddug.com.naukoteka.ui.chat


import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import uddug.com.domain.entities.chat.Chat
import uddug.com.naukoteka.R
import uddug.com.naukoteka.mvvm.chat.ChatListEvents
import uddug.com.naukoteka.mvvm.chat.ChatListUiState
import uddug.com.naukoteka.mvvm.chat.ChatListViewModel
import uddug.com.naukoteka.presentation.profile.navigation.ContainerNavigationView
import uddug.com.naukoteka.ui.chat.ChatDialogFragment.Companion.DIALOG_ID
import uddug.com.naukoteka.ui.chat.ChatDetailDialogFragment
import uddug.com.naukoteka.ui.chat.compose.ChatListComponent

@AndroidEntryPoint
class ChatListFragment : Fragment() {

    private var navigationView: ContainerNavigationView? = null

    private val viewModel: ChatListViewModel by viewModels()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        navigationView = requireActivity() as ContainerNavigationView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()
        viewModel.loadFolders()

        findNavController().currentBackStackEntry?.savedStateHandle
            ?.getLiveData<Boolean>("refreshChats")
            ?.observe(viewLifecycleOwner) { shouldRefresh ->
                if (shouldRefresh) {
                    viewModel.refreshChats()
                    findNavController().currentBackStackEntry?.savedStateHandle?.remove<Boolean>("refreshChats")
                }
            }
    }




    override fun onResume() {
        super.onResume()
        navigationView?.showNavigationBottomBar(true)
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                when (state) {
                    is ChatListUiState.Loading -> showLoading()
                    is ChatListUiState.Success -> showChats(state.chats)
                    is ChatListUiState.Error -> showError(state.message)
                    else -> {}
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.events.collectLatest { state ->
                when (state) {
                    is ChatListEvents.OpenDialogDetail -> {
                        findNavController().navigate(
                            R.id.chatDialogFragment,
                            args = Bundle().apply {
                                putLong(DIALOG_ID, state.dialogId)
                            }
                        )
                    }

                    ChatListEvents.OpenCreateDialog -> {
                        findNavController().navigate(
                            R.id.chatCreateSingleFragment,
                            args = Bundle().apply {
                             //   putLong(DIALOG_ID, state.dialogId)
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    ChatListComponent(
                        viewModel = viewModel,
                        onCreateChatClick = {
                            viewModel.onClickCreateDialog()
                        },
                        onBackPressed = {
                            findNavController().popBackStack()
                        },
                        onShowAttachments = { dialogId ->
                            findNavController().navigate(
                                R.id.chatDetailDialog,
                                args = Bundle().apply {
                                    putLong(ChatDetailDialogFragment.DIALOG_ID, dialogId)
                                }
                            )
                        }
                    )
                }
            }
        }
    }

    private fun showLoading() {

    }

    private fun showChats(chats: List<Chat>) {

    }

    private fun showError(message: String) {

    }


}