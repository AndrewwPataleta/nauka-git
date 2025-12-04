package uddug.com.naukoteka.ui.chat

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
import uddug.com.naukoteka.mvvm.chat.ChatCreatePollEvent
import uddug.com.naukoteka.mvvm.chat.ChatCreatePollViewModel
import uddug.com.naukoteka.ui.chat.compose.ChatCreatePollScreen
import uddug.com.naukoteka.ui.chat.ChatDialogFragment.Companion.CREATED_POLL_ID_KEY
import uddug.com.naukoteka.ui.theme.NaukotekaTheme

@AndroidEntryPoint
class ChatCreatePollFragment : Fragment() {

    private val viewModel: ChatCreatePollViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.events.collectLatest { event ->
                when (event) {
                    is ChatCreatePollEvent.ValidationError -> {
                        val message = event.message
                            ?: event.messageResId?.let { getString(it) }
                            ?: getString(R.string.chat_create_poll_validation_error)
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                    }
                    is ChatCreatePollEvent.PollCreated -> {
                        findNavController().previousBackStackEntry?.savedStateHandle?.set(
                            CREATED_POLL_ID_KEY,
                            event.poll.id
                        )
                        Toast.makeText(
                            requireContext(),
                            R.string.chat_create_poll_created_success,
                            Toast.LENGTH_SHORT
                        ).show()
                        findNavController().popBackStack()
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
                    ChatCreatePollScreen(
                        viewModel = viewModel,
                        onBack = { requireActivity().onBackPressed() }
                    )
                }
            }
        }
    }
}
