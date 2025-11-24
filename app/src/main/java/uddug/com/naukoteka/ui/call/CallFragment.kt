package uddug.com.naukoteka.ui.call

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import uddug.com.naukoteka.mvvm.call.CallViewModel
import uddug.com.naukoteka.ui.call.compose.CallScreen

@AndroidEntryPoint
class CallFragment : Fragment() {

    private val viewModel: CallViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val contactName = arguments?.getString(ARG_CONTACT_NAME)
        val avatarUrl = arguments?.getString(ARG_AVATAR_URL)

        viewModel.startCall(requireActivity(), contactName, avatarUrl)

        return ComposeView(requireContext()).apply {
            setContent {
                val state = viewModel.uiState.collectAsState().value
                MaterialTheme {
                    CallScreen(
                        state = state,
                        onBackPressed = { findNavController().popBackStack() },
                        onEndCall = {
                            viewModel.endCall()
                            findNavController().popBackStack()
                        }
                    )
                }
            }
        }
    }

    companion object {
        const val ARG_CONTACT_NAME = "contact_name"
        const val ARG_AVATAR_URL = "avatar_url"
    }
}
