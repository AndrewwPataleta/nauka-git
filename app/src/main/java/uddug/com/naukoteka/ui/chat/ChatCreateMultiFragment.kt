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
import uddug.com.naukoteka.R
import uddug.com.naukoteka.mvvm.chat.ChatCreateMultiEvent
import uddug.com.naukoteka.mvvm.chat.ChatCreateMultiViewModel
import uddug.com.naukoteka.presentation.profile.navigation.ContainerNavigationView
import uddug.com.naukoteka.ui.chat.compose.ChatCreateMultiScreen

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.events.collectLatest { event ->
                when (event) {
                    is ChatCreateMultiEvent.GroupCreated -> {
                        findNavController().getBackStackEntry(R.id.chatListFragment)
                            .savedStateHandle["refreshChats"] = true
                        findNavController().popBackStack(R.id.chatListFragment, false)
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
}

