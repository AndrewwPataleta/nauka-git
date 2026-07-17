package uddug.com.naukoteka.ui.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import uddug.com.naukoteka.R
import uddug.com.naukoteka.mvvm.chat.ForwardMessageItem
import uddug.com.naukoteka.mvvm.chat.ForwardMessageViewModel
import uddug.com.naukoteka.ui.chat.compose.ForwardMessageComponent
import uddug.com.naukoteka.ui.theme.NaukotekaTheme

@AndroidEntryPoint
class ForwardMessageFragment : Fragment() {

    private val viewModel: ForwardMessageViewModel by viewModels()

    private var messageIds: List<Long> = emptyList()
    private var forwardText: String? = null
    private var forwardAuthor: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val singleId = arguments?.getLong(ARG_MESSAGE_ID, 0L) ?: 0L
        val multipleIds = arguments?.getLongArray(ARG_MESSAGE_IDS)
        messageIds = when {
            multipleIds != null && multipleIds.isNotEmpty() -> multipleIds.toList()
            singleId != 0L -> listOf(singleId)
            else -> emptyList()
        }
        forwardText = arguments?.getString(ARG_FORWARD_TEXT)
        forwardAuthor = arguments?.getString(ARG_FORWARD_AUTHOR)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                NaukotekaTheme {
                    ForwardMessageComponent(
                        viewModel = viewModel,
                        forwardAuthor = forwardAuthor,
                        forwardText = forwardText,
                        onBack = { requireActivity().onBackPressed() },
                        onSelect = { item -> onDialogSelected(item) }
                    )
                }
            }
        }
    }


    private fun onDialogSelected(item: ForwardMessageItem) {
        val navOptions = NavOptions.Builder()
            .setPopUpTo(R.id.chatDialogFragment, true)
            .build()
        findNavController().navigate(
            R.id.chatDialogFragment,
            Bundle().apply {
                putLong(ChatDialogFragment.DIALOG_ID, item.dialogId)
                putLongArray(ChatDialogFragment.ARG_PENDING_FORWARD_IDS, messageIds.toLongArray())
                putString(ChatDialogFragment.ARG_PENDING_FORWARD_TEXT, forwardText)
                putString(ChatDialogFragment.ARG_PENDING_FORWARD_AUTHOR, forwardAuthor)
            },
            navOptions
        )
    }

    companion object {
        const val ARG_MESSAGE_ID = "forward_message_id"
        const val ARG_MESSAGE_IDS = "forward_message_ids"
        const val ARG_FORWARD_TEXT = "forward_message_text"
        const val ARG_FORWARD_AUTHOR = "forward_message_author"
    }
}
