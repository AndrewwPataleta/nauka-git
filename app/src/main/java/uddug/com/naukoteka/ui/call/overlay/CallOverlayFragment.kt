package uddug.com.naukoteka.ui.call.overlay

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import dagger.hilt.android.AndroidEntryPoint
import uddug.com.naukoteka.R
import uddug.com.naukoteka.mvvm.call.CallViewModel
import uddug.com.naukoteka.ui.call.CallFragment
import kotlin.math.roundToInt

@AndroidEntryPoint
class CallOverlayFragment : Fragment() {

    private val viewModel: CallViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT,
                Gravity.BOTTOM or Gravity.END,
            )
            setContent {
                val state = viewModel.uiState.collectAsState().value

                MaterialTheme {
                    CallOverlay(
                        state = state,
                        onExpand = {
                            openFullScreen()
                            removeSelf()
                        },
                        onEndCall = {
                            viewModel.endCall()
                            removeSelf()
                        },
                        onClose = { removeSelf() },
                        onFinished = { removeSelf() }
                    )
                }
            }
        }
    }

    private fun openFullScreen() {
        val navController = requireActivity().findNavController(R.id.main_nav_host_fragment)
        val args = Bundle().apply {
            putLong(CallFragment.ARG_DIALOG_ID, viewModel.uiState.value.dialogId ?: 0L)
            putString(CallFragment.ARG_CALL_TITLE, viewModel.uiState.value.callTitle)
            putParcelableArrayList(
                CallFragment.ARG_PARTICIPANTS,
                ArrayList(viewModel.uiState.value.participants),
            )
        }
        navController.navigate(R.id.callFragment, args)
    }

    private fun removeSelf() {
        parentFragmentManager.beginTransaction()
            .remove(this)
            .commitAllowingStateLoss()
    }

    companion object {
        const val TAG = "CallOverlayFragment"
    }
}
