package uddug.com.naukoteka.ui.chat

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import uddug.com.domain.entities.chat.Chat
import uddug.com.domain.entities.profile.UserProfileFullInfo
import uddug.com.naukoteka.R
import uddug.com.naukoteka.mvvm.chat.ChatDialogEvents
import uddug.com.naukoteka.mvvm.chat.ChatDialogUiState
import uddug.com.naukoteka.mvvm.chat.ChatDialogViewModel
import uddug.com.naukoteka.mvvm.chat.ChatListEvents
import uddug.com.naukoteka.mvvm.chat.ChatListUiState
import uddug.com.naukoteka.mvvm.chat.ChatListViewModel
import uddug.com.naukoteka.presentation.profile.navigation.ContainerNavigationView
import uddug.com.naukoteka.ui.chat.ChatDetailDialogFragment.Companion.DIALOG_DETAIL
import uddug.com.naukoteka.ui.chat.compose.ChatDialogComponent
import uddug.com.naukoteka.ui.chat.compose.ChatListComponent

@AndroidEntryPoint
class ChatDialogFragment : Fragment() {

    private var navigationView: ContainerNavigationView? = null

    private val viewModel: ChatDialogViewModel by viewModels()

    private var dialogId: Long = 0

    override fun onAttach(context: Context) {
        super.onAttach(context)
        navigationView = requireActivity() as ContainerNavigationView
    }

    override fun onResume() {
        super.onResume()
        navigationView?.showNavigationBottomBar(false)
    }

    companion object {
        const val DIALOG_ID = "DIALOG_ID"
        const val INTERLOCUTOR_ID = "INTERLOCUTOR_ID"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
        dialogId = arguments?.getLong(DIALOG_ID) ?: 0
        val peerId = arguments?.getString(INTERLOCUTOR_ID)
        if (dialogId != 0L) {
            viewModel.loadMessages(dialogId)
        } else if (!peerId.isNullOrEmpty()) {
            viewModel.loadMessagesByPeer(peerId)
        }
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                when (state) {
                    else -> {}
                }
            }
        }
        lifecycleScope.launch {
            viewModel.events.collectLatest { state ->
                when (state) {
                    is ChatDialogEvents.OpenChatProfileDetail -> {
                        val isGroup = (state.dialogInfo.users?.size ?: 0) > 2
                        val destination = if (isGroup) {
                            R.id.chatGroupDetailFragment
                        } else {
                            R.id.chatDetailDialog
                        }
                        findNavController().navigate(
                            destination,
                            args = Bundle().apply {
                                putLong(DIALOG_ID, state.dialogId)
                                putParcelable(DIALOG_DETAIL, state.dialogInfo)
                            }
                        )
                    }
                }
            }
        }

        findNavController().currentBackStackEntry?.savedStateHandle
            ?.getLiveData<UserProfileFullInfo>("selectedUser")
            ?.observe(viewLifecycleOwner) { user ->
                user?.let { viewModel.attachUserContact(it) }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    ChatDialogComponent(
                        viewModel = viewModel,
                        onBackPressed = {
                            requireActivity().onBackPressed()
                        },
                        onSearchClick = {
                            findNavController().navigate(
                                R.id.chatDetailSearchFragment,
                                Bundle().apply { putLong(ChatDetailDialogFragment.DIALOG_ID, dialogId) }
                            )
                        },
                        onContactClick = {
                            findNavController().navigate(R.id.sendContactFragment)
                        }
                    )
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_UNSPECIFIED)
    }
}