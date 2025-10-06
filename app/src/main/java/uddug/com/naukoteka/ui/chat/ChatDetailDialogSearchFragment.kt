package uddug.com.naukoteka.ui.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import uddug.com.naukoteka.R
import uddug.com.naukoteka.mvvm.chat.ChatDialogDetailViewModel
import uddug.com.naukoteka.ui.chat.compose.ChatDetailDialogSearchComponent

@AndroidEntryPoint
class ChatDetailDialogSearchFragment : Fragment() {

    private val viewModel: ChatDialogDetailViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        arguments?.getLong(ChatDetailDialogFragment.DIALOG_ID)?.let { viewModel.loadDialogInfo(it) }

        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    ChatDetailDialogSearchComponent(
                        viewModel = viewModel,
                        onBackPressed = { requireActivity().onBackPressed() },
                        onMessageSelected = { dialogId, messageId ->
                            val args = Bundle().apply {
                                putLong(ChatDialogFragment.DIALOG_ID, dialogId)
                                putLong(ChatDialogFragment.MESSAGE_ID, messageId)
                            }
                            findNavController().navigate(R.id.chatDialogFragment, args)
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
