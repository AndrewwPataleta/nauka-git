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
import uddug.com.naukoteka.ui.chat.ChatCreateMultiFragment

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
        navigationView?.showNavigationBottomBar(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
        observeParticipantsResult()
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.events.collectLatest { event ->
                when (event) {
                    is ChatEditGroupEvent.GroupUpdated -> {
                        findNavController().previousBackStackEntry?.savedStateHandle?.apply {
                            set("refreshDialogInfo", true)
                            set("refreshChats", true)
                        }
                        findNavController().popBackStack()
                    }

                    ChatEditGroupEvent.ShowImageUploadError -> Unit
                    ChatEditGroupEvent.ShowMissingParticipantsError -> Unit
                    is ChatEditGroupEvent.ShowError -> Unit
                }
            }
        }
    }

    private fun observeParticipantsResult() {
        findNavController().currentBackStackEntry?.savedStateHandle
            ?.getLiveData<ArrayList<UserProfileFullInfo>>(ChatCreateGroupFragment.SELECTED_USERS_ARG)
            ?.observe(viewLifecycleOwner) { users ->
                if (users != null) {
                    viewModel.onParticipantsAdded(users)
                    findNavController().currentBackStackEntry?.savedStateHandle
                        ?.set(ChatCreateGroupFragment.SELECTED_USERS_ARG, null)
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
                                putBoolean(ChatCreateMultiFragment.RETURN_RESULT_KEY, true)
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
        const val ARG_DIALOG_ID = ChatEditGroupViewModel.ARG_DIALOG_ID
    }
}
