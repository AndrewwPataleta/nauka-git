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
import androidx.core.os.bundleOf
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import uddug.com.naukoteka.R
import uddug.com.naukoteka.mvvm.chat.ChatCreateMultiEvent
import uddug.com.naukoteka.mvvm.chat.ChatCreateMultiViewModel
import uddug.com.naukoteka.presentation.profile.navigation.ContainerNavigationView
import uddug.com.naukoteka.ui.chat.compose.ChatCreateMultiScreen
import uddug.com.naukoteka.ui.chat.ChatCreateGroupFragment

@AndroidEntryPoint
class ChatCreateMultiFragment : Fragment() {

    private var navigationView: ContainerNavigationView? = null

    private val viewModel: ChatCreateMultiViewModel by viewModels()

    private val returnResult: Boolean
        get() = arguments?.getBoolean(RETURN_RESULT_KEY) ?: false

    override fun onAttach(context: Context) {
        super.onAttach(context)
        navigationView = requireActivity() as ContainerNavigationView
    }

    override fun onResume() {
        super.onResume()
        navigationView?.showNavigationBottomBar(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.events.collectLatest { event ->
                when (event) {
                    is ChatCreateMultiEvent.OpenGroupDetails -> {
                        if (returnResult) {
                            findNavController().previousBackStackEntry?.savedStateHandle?.set(
                                ChatCreateGroupFragment.SELECTED_USERS_ARG,
                                ArrayList(event.selectedUsers)
                            )
                            findNavController().popBackStack()
                        } else {
                            val args = bundleOf(
                                ChatCreateGroupFragment.SELECTED_USERS_ARG to ArrayList(event.selectedUsers)
                            )
                            findNavController().navigate(R.id.chatCreateGroupFragment, args)
                        }
                    }
                    ChatCreateMultiEvent.ShowMinimumMembersError -> {
                        Toast.makeText(
                            requireContext(),
                            R.string.chat_create_group_min_members_error,
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
        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    ChatCreateMultiScreen(
                        viewModel = viewModel,
                        onBackPressed = { findNavController().popBackStack() }
                    )
                }
            }
        }
    }

    companion object {
        const val RETURN_RESULT_KEY = "return_result"
        const val MIN_SELECTION_ARG = ChatCreateMultiViewModel.MIN_SELECTION_KEY
    }
}

