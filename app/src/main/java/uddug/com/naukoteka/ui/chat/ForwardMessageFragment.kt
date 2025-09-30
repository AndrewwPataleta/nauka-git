package uddug.com.naukoteka.ui.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import uddug.com.naukoteka.R
import uddug.com.naukoteka.mvvm.chat.ForwardMessageEvent
import uddug.com.naukoteka.mvvm.chat.ForwardMessageItem
import uddug.com.naukoteka.mvvm.chat.ForwardMessageViewModel
import uddug.com.naukoteka.ui.chat.compose.ForwardMessageComponent

@AndroidEntryPoint
class ForwardMessageFragment : Fragment() {

    private val viewModel: ForwardMessageViewModel by viewModels()

    private var messageId: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        messageId = requireArguments().getLong(ARG_MESSAGE_ID)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    ForwardMessageComponent(
                        viewModel = viewModel,
                        onBack = { requireActivity().onBackPressed() },
                        onSelect = { item -> onDialogSelected(item) }
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collectLatest { event ->
                    when (event) {
                        is ForwardMessageEvent.ForwardSuccess -> navigateToDialog(event.dialogId)
                        ForwardMessageEvent.ForwardError -> showError()
                    }
                }
            }
        }
    }

    private fun onDialogSelected(item: ForwardMessageItem) {
        viewModel.forwardMessage(messageId, item.dialogId)
    }

    private fun navigateToDialog(dialogId: Long) {
        val navOptions = NavOptions.Builder()
            .setPopUpTo(R.id.chatDialogFragment, true)
            .build()
        findNavController().navigate(
            R.id.chatDialogFragment,
            Bundle().apply { putLong(ChatDialogFragment.DIALOG_ID, dialogId) },
            navOptions
        )
    }

    private fun showError() {
        Toast.makeText(
            requireContext(),
            getString(R.string.forward_message_error),
            Toast.LENGTH_SHORT
        ).show()
    }

    companion object {
        const val ARG_MESSAGE_ID = "forward_message_id"
    }
}
