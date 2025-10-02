package uddug.com.naukoteka.ui.chat

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import uddug.com.naukoteka.R
import uddug.com.naukoteka.mvvm.chat.ChatCreateMultiEvent
import uddug.com.naukoteka.mvvm.chat.ChatCreateMultiViewModel
import uddug.com.naukoteka.mvvm.chat.MIN_SELECTION_KEY
import uddug.com.naukoteka.presentation.profile.navigation.ContainerNavigationView
import uddug.com.naukoteka.ui.chat.compose.ChatCreateMultiScreen
import uddug.com.naukoteka.ui.chat.ChatCreateGroupFragment

@AndroidEntryPoint
class ChatCreateMultiFragment : Fragment() {

    private var navigationView: ContainerNavigationView? = null

    private val viewModel: ChatCreateMultiViewModel by viewModels()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        navigationView = requireActivity() as ContainerNavigationView
    }

    override fun onResume() {
        super.onResume()
        navigationView?.showNavigationBottomBar(true)
    }

    private val mode: String
        get() = arguments?.getString(MODE_ARG) ?: MODE_CREATE

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.events.collectLatest { event ->
                when (event) {
                    is ChatCreateMultiEvent.OpenGroupDetails -> {
                        if (mode == MODE_EDIT) {
                            findNavController().previousBackStackEntry?.savedStateHandle
                                ?.set(ChatEditGroupFragment.SELECTED_USERS_ARG, ArrayList(event.selectedUsers))
                            findNavController().popBackStack()
                        } else {
                            val args = bundleOf(
                                ChatCreateGroupFragment.SELECTED_USERS_ARG to ArrayList(event.selectedUsers)
                            )
                            findNavController().navigate(R.id.chatCreateGroupFragment, args)
                        }
                    }
                    is ChatCreateMultiEvent.ShowMinimumMembersError -> {
                        val messageRes = if (event.minSelection <= 1) {
                            R.string.chat_edit_group_min_selection_error
                        } else {
                            R.string.chat_create_group_min_members_error
                        }
                        Toast.makeText(
                            requireContext(),
                            messageRes,
                            Toast.LENGTH_SHORT
                        ).show()
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
        val titleRes = if (mode == MODE_EDIT) {
            R.string.chat_edit_group_add_members_title
        } else {
            R.string.chat_create_single_title
        }

        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    ChatCreateMultiScreen(
                        viewModel = viewModel,
                        onBackPressed = { findNavController().popBackStack() },
                        titleRes = titleRes
                    )
                }
            }
        }
    }

    companion object {
        const val MODE_ARG = "chat_create_multi_mode"
        const val MODE_CREATE = "create"
        const val MODE_EDIT = "edit"
        const val MIN_SELECTION_ARG = MIN_SELECTION_KEY
    }
}

