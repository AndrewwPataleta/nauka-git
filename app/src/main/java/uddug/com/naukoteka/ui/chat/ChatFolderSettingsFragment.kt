package uddug.com.naukoteka.ui.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import uddug.com.naukoteka.R
import uddug.com.naukoteka.mvvm.chat.ChatListViewModel
import uddug.com.naukoteka.ui.chat.compose.ChatFolderSettingsComponent

@AndroidEntryPoint
class ChatFolderSettingsFragment : Fragment() {

    private val viewModel: ChatListViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    ChatFolderSettingsComponent(
                        viewModel = viewModel,
                        onBackPressed = { findNavController().popBackStack() },
                        onCreateFolderClick = { findNavController().navigate(R.id.chatCreateFolderFragment) }
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.loadFolders()
        observeRefreshResult()
    }

    private fun observeRefreshResult() {
        findNavController().currentBackStackEntry?.savedStateHandle
            ?.getLiveData<Boolean>(ChatCreateFolderFragment.REFRESH_FOLDERS_KEY)
            ?.observe(viewLifecycleOwner) { shouldRefresh ->
                if (shouldRefresh == true) {
                    viewModel.loadFolders()
                    findNavController().currentBackStackEntry?.savedStateHandle
                        ?.set(ChatCreateFolderFragment.REFRESH_FOLDERS_KEY, null)
                }
            }
    }
}
