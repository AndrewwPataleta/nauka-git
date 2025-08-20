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
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import uddug.com.domain.entities.chat.DialogInfo
import uddug.com.naukoteka.mvvm.chat.ChatGroupDetailViewModel
import uddug.com.naukoteka.presentation.profile.navigation.ContainerNavigationView
import uddug.com.naukoteka.ui.chat.compose.ChatGroupDetailComponent

@AndroidEntryPoint
class ChatGroupDetailFragment : Fragment() {

    private var navigationView: ContainerNavigationView? = null

    private val viewModel: ChatGroupDetailViewModel by viewModels()

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
                    ChatGroupDetailComponent(
                        viewModel = viewModel,
                        onBackPressed = { requireActivity().onBackPressed() }
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
