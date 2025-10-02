package uddug.com.naukoteka.ui.chat

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import uddug.com.domain.entities.chat.DialogInfo
import uddug.com.domain.entities.profile.UserProfileFullInfo
import uddug.com.naukoteka.R
import uddug.com.naukoteka.mvvm.chat.ChatGroupDetailEvent
import uddug.com.naukoteka.mvvm.chat.ChatGroupDetailUiState
import uddug.com.naukoteka.mvvm.chat.ChatGroupDetailViewModel
import uddug.com.naukoteka.presentation.profile.navigation.ContainerNavigationView
import uddug.com.naukoteka.ui.chat.compose.ChatGroupDetailComponent

@AndroidEntryPoint
class ChatGroupDetailFragment : Fragment() {

    private var navigationView: ContainerNavigationView? = null

    private val viewModel: ChatGroupDetailViewModel by viewModels()

    private var dialogId: Long = 0

    override fun onAttach(context: Context) {
        super.onAttach(context)
        navigationView = requireActivity() as ContainerNavigationView
    }

    override fun onResume() {
        super.onResume()
        navigationView?.showNavigationBottomBar(false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialogId = arguments?.getLong(DIALOG_ID) ?: dialogId
        setupObservers()
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.uiState.collectLatest { _ ->
                // Reserved for future UI state handling
            }
        }

        lifecycleScope.launch {
            viewModel.events.collectLatest { event ->
                when (event) {
                    is ChatGroupDetailEvent.GroupDeleted -> {
                        findNavController().getBackStackEntry(R.id.chatListFragment)
                            .savedStateHandle["refreshChats"] = true
                        findNavController().popBackStack(R.id.chatListFragment, false)
                    }

                    is ChatGroupDetailEvent.NotificationsUpdated -> {
                        val messageRes = if (event.disabled) {
                            R.string.chat_disable_notifications
                        } else {
                            R.string.chat_enable_notifications
                        }
                        Toast.makeText(requireContext(), messageRes, Toast.LENGTH_SHORT).show()
                    }

                    is ChatGroupDetailEvent.ShowError -> {
                        Toast.makeText(requireContext(), event.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        findNavController().currentBackStackEntry?.savedStateHandle
            ?.getLiveData<DialogInfo>(UPDATED_DIALOG_INFO)
            ?.observe(viewLifecycleOwner) { info ->
                if (info != null) {
                    dialogId = info.id
                    viewModel.setDialogInfo(info)
                    findNavController().currentBackStackEntry?.savedStateHandle
                        ?.remove<DialogInfo>(UPDATED_DIALOG_INFO)
                }
            }

        findNavController().currentBackStackEntry?.savedStateHandle
            ?.getLiveData<ArrayList<UserProfileFullInfo>>(ChatEditGroupFragment.SELECTED_USERS_ARG)
            ?.observe(viewLifecycleOwner) { users ->
                if (!users.isNullOrEmpty()) {
                    viewModel.addParticipants(users)
                }
                findNavController().currentBackStackEntry?.savedStateHandle
                    ?.remove<ArrayList<UserProfileFullInfo>>(ChatEditGroupFragment.SELECTED_USERS_ARG)
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        arguments?.getParcelable<DialogInfo>(DIALOG_DETAIL)
            ?.let {
                dialogId = it.id
                viewModel.setDialogInfo(it)
            }
            ?: arguments?.getLong(DIALOG_ID)?.let {
                dialogId = it
                viewModel.loadDialogInfo(it)
            }

        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    ChatGroupDetailComponent(
                        viewModel = viewModel,
                        onBackPressed = { requireActivity().onBackPressed() },
                        onSearchClick = {
                            val currentDialogId = (viewModel.uiState.value as? ChatGroupDetailUiState.Success)?.dialogId ?: dialogId
                            findNavController().navigate(
                                R.id.chatDetailSearchFragment,
                                Bundle().apply { putLong(ChatDetailDialogFragment.DIALOG_ID, currentDialogId) }
                            )
                        },
                        onAddParticipantsClick = {
                            val args = Bundle().apply {
                                putString(ChatCreateMultiFragment.MODE_ARG, ChatCreateMultiFragment.MODE_EDIT)
                                putInt(ChatCreateMultiFragment.MIN_SELECTION_ARG, 1)
                            }
                            findNavController().navigate(R.id.chatCreateMultiFragment, args)
                        },
                        onEditGroupClick = { id ->
                            val args = Bundle().apply {
                                putLong(ChatEditGroupFragment.DIALOG_ID, id)
                            }
                            findNavController().navigate(R.id.chatEditGroupFragment, args)
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

    companion object {
        const val DIALOG_ID = "DIALOG_ID"
        const val DIALOG_DETAIL = "DIALOG_DETAIL"
        const val UPDATED_DIALOG_INFO = "UPDATED_DIALOG_INFO"
    }
}
