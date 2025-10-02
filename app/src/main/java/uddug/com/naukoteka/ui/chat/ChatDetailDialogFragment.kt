package uddug.com.naukoteka.ui.chat

import android.content.Context
import android.content.Intent
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
import uddug.com.domain.entities.chat.DialogInfo
import uddug.com.naukoteka.mvvm.chat.ChatDialogDetailViewModel
import uddug.com.naukoteka.mvvm.chat.ChatDetailUiState
import uddug.com.naukoteka.mvvm.chat.ChatDialogDetailEvent
import uddug.com.naukoteka.R
import uddug.com.naukoteka.presentation.profile.navigation.ContainerNavigationView
import uddug.com.naukoteka.ui.chat.compose.ChatDetailDialogComponent
import uddug.com.naukoteka.ui.chat.ChatAvatarPreviewFragment.Companion.ARG_AVATAR_PATH
@AndroidEntryPoint
class ChatDetailDialogFragment : Fragment() {

    private var navigationView: ContainerNavigationView? = null

    private val viewModel: ChatDialogDetailViewModel by viewModels()

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
        const val DIALOG_DETAIL = "DIALOG_DETAIL"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
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
            viewModel.events.collectLatest { event ->
                when (event) {
                    is ChatDialogDetailEvent.Share -> {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, event.link)
                        }
                        startActivity(Intent.createChooser(shareIntent, getString(R.string.share)))
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
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        arguments?.getParcelable<DialogInfo>(DIALOG_DETAIL)
            ?.let { viewModel.setDialogInfo(it) }
            ?: arguments?.getLong(DIALOG_ID)?.let { viewModel.loadDialogInfo(it) }

        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    ChatDetailDialogComponent(
                        viewModel = viewModel,
                        onBackPressed = {
                            requireActivity().onBackPressed()
                        },
                        onNavigateToProfile = {
                            viewModel.getCurrentUser()?.let { navigationView?.selectShowEditFragment(it) }
                        },
                        onSearchClick = {
                            val dialogId = (viewModel.uiState.value as? ChatDetailUiState.Success)?.dialogId
                                ?: arguments?.getLong(DIALOG_ID)
                                ?: 0L
                            findNavController().navigate(
                                R.id.chatDetailSearchFragment,
                                Bundle().apply { putLong(DIALOG_ID, dialogId) }
                            )
                        },
                        onChatDeleted = {
                            findNavController().popBackStack(R.id.chatListFragment, false)
                        },
                        onViewAvatar = { avatarPath ->
                            findNavController().navigate(
                                R.id.chatAvatarPreviewFragment,
                                Bundle().apply {
                                    putString(ARG_AVATAR_PATH, avatarPath)
                                }
                            )
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