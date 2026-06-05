package uddug.com.naukoteka.ui.call.overlay

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import dagger.hilt.android.AndroidEntryPoint
import uddug.com.naukoteka.R
import uddug.com.naukoteka.mvvm.call.CallViewModel
import uddug.com.naukoteka.ui.call.GroupCallFragment
import uddug.com.naukoteka.ui.call.SingleCallFragment
import uddug.com.naukoteka.ui.theme.NaukotekaTheme
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

                NaukotekaTheme {
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
                        onFinished = { removeSelf() }
                    )
                }
            }
        }
    }

    private fun openFullScreen() {
        runCatching {
            val navController = requireActivity().findNavController(R.id.main_nav_host_fragment)
            val state = viewModel.uiState.value

            if (state.isGroupCall) {
                val args = Bundle().apply {
                    putString(GroupCallFragment.ARG_CONTACT_NAME, state.callTitle)
                    putLong(GroupCallFragment.ARG_DIALOG_ID, state.dialogId ?: 0L)
                    putBoolean(GroupCallFragment.ARG_IS_VIDEO_CALL, state.isVideoCall)
                }
                navController.navigate(R.id.groupCallFragment, args)
            } else {
                val args = Bundle().apply {
                    putLong(SingleCallFragment.ARG_DIALOG_ID, state.dialogId ?: 0L)
                    putString(SingleCallFragment.ARG_CALL_TITLE, state.callTitle)
                    putParcelableArrayList(
                        SingleCallFragment.ARG_PARTICIPANTS,
                        ArrayList(state.participants),
                    )
                    putBoolean(SingleCallFragment.ARG_IS_VIDEO_CALL, state.isVideoCall)
                }
                navController.navigate(R.id.singleCallFragment, args)
            }
        }
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
