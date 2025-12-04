package uddug.com.naukoteka.ui.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import uddug.com.naukoteka.mvvm.chat.ChatPollResultsViewModel
import uddug.com.naukoteka.ui.chat.compose.ChatPollResultsScreen
import uddug.com.naukoteka.ui.theme.NaukotekaTheme

@AndroidEntryPoint
class ChatPollResultsFragment : Fragment() {

    private val viewModel: ChatPollResultsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                NaukotekaTheme {
                    ChatPollResultsScreen(
                        viewModel = viewModel,
                        onBack = { requireActivity().onBackPressed() }
                    )
                }
            }
        }
    }

    companion object {
        const val ARG_POLL_ID = "pollId"
    }
}
