package uddug.com.naukoteka.ui.chat

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import uddug.com.naukoteka.R
import uddug.com.naukoteka.mvvm.chat.ChatCreateGroupEvent
import uddug.com.naukoteka.mvvm.chat.ChatCreateGroupViewModel
import uddug.com.naukoteka.presentation.profile.navigation.ContainerNavigationView
import uddug.com.naukoteka.ui.chat.compose.ChatCreateGroupScreen
import uddug.com.naukoteka.ui.theme.NaukotekaTheme

@AndroidEntryPoint
class ChatCreateGroupFragment : Fragment() {

    private val viewModel: ChatCreateGroupViewModel by viewModels()

    private var navigationView: ContainerNavigationView? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        navigationView = requireActivity() as? ContainerNavigationView
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
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.events.collectLatest { event ->
                when (event) {
                    is ChatCreateGroupEvent.GroupCreated -> {
                        findNavController().getBackStackEntry(R.id.chatListFragment)
                            .savedStateHandle["refreshChats"] = true
                        findNavController().popBackStack(R.id.chatListFragment, false)
                    }

                    ChatCreateGroupEvent.ShowImageUploadError -> {
                        Toast.makeText(
                            requireContext(),
                            R.string.chat_create_group_image_upload_error,
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    ChatCreateGroupEvent.ShowMissingParticipantsError -> {
                        Toast.makeText(
                            requireContext(),
                            R.string.chat_create_group_missing_participants_error,
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    is ChatCreateGroupEvent.ShowError -> {
                        val message = event.message ?: getString(R.string.chat_create_group_generic_error)
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                NaukotekaTheme {
                    ChatCreateGroupScreen(
                        viewModel = viewModel,
                        onBackPressed = { findNavController().popBackStack() },
                        onAddParticipantsClick = { findNavController().popBackStack() }
                    )
                }
            }
        }
    }

    override fun onDetach() {
        super.onDetach()
        navigationView = null
    }

    companion object {
        const val SELECTED_USERS_ARG = ChatCreateGroupViewModel.SELECTED_USERS_KEY
    }
}
