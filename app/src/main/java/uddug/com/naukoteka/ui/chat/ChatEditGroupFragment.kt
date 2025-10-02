package uddug.com.naukoteka.ui.chat

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import uddug.com.domain.entities.profile.UserProfileFullInfo
import uddug.com.naukoteka.R
import uddug.com.naukoteka.mvvm.chat.ChatEditGroupEvent
import uddug.com.naukoteka.mvvm.chat.ChatEditGroupViewModel
import uddug.com.naukoteka.presentation.profile.navigation.ContainerNavigationView
import uddug.com.naukoteka.ui.chat.compose.ChatEditGroupScreen

@AndroidEntryPoint
class ChatEditGroupFragment : Fragment() {

    private val viewModel: ChatEditGroupViewModel by viewModels()

    private var navigationView: ContainerNavigationView? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        navigationView = requireActivity() as? ContainerNavigationView
    }

    override fun onResume() {
        super.onResume()
        navigationView?.showNavigationBottomBar(false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.events.collectLatest { event ->
                when (event) {
                    is ChatEditGroupEvent.GroupUpdated -> {
                        event.dialogInfo?.let {
                            findNavController().previousBackStackEntry?.savedStateHandle
                                ?.set(ChatGroupDetailFragment.UPDATED_DIALOG_INFO, it)
                        }
                        findNavController().popBackStack()
                    }

                    is ChatEditGroupEvent.ShowError -> {
                        Toast.makeText(requireContext(), event.message, Toast.LENGTH_SHORT).show()
                    }

                    ChatEditGroupEvent.ShowImageUploadError -> {
                        Toast.makeText(
                            requireContext(),
                            R.string.chat_create_group_image_upload_error,
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    ChatEditGroupEvent.ShowMissingParticipantsError -> {
                        Toast.makeText(
                            requireContext(),
                            R.string.chat_create_group_missing_participants_error,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

        findNavController().currentBackStackEntry?.savedStateHandle
            ?.getLiveData<ArrayList<UserProfileFullInfo>>(SELECTED_USERS_ARG)
            ?.observe(viewLifecycleOwner) { users ->
                if (users != null) {
                    viewModel.onMembersSelected(users)
                    findNavController().currentBackStackEntry?.savedStateHandle
                        ?.remove<ArrayList<UserProfileFullInfo>>(SELECTED_USERS_ARG)
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
                MaterialTheme {
                    ChatEditGroupScreen(
                        viewModel = viewModel,
                        onBackPressed = { findNavController().popBackStack() },
                        onAddParticipantsClick = {
                            val args = Bundle().apply {
                                putString(ChatCreateMultiFragment.MODE_ARG, ChatCreateMultiFragment.MODE_EDIT)
                                putInt(ChatCreateMultiFragment.MIN_SELECTION_ARG, 1)
                            }
                            findNavController().navigate(R.id.chatCreateMultiFragment, args)
                        }
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
        const val DIALOG_ID = ChatEditGroupViewModel.DIALOG_ID_KEY
        const val SELECTED_USERS_ARG = "chat_edit_group_selected_users"
    }
}
