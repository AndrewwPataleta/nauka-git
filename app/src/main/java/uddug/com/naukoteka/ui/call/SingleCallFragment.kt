package uddug.com.naukoteka.ui.call

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import uddug.com.naukoteka.mvvm.call.CallParticipant
import uddug.com.naukoteka.mvvm.call.CallStatus
import uddug.com.naukoteka.mvvm.call.CallViewModel
import uddug.com.naukoteka.ui.activities.main.ContainerActivity
import uddug.com.naukoteka.ui.call.compose.CallScreen
import uddug.com.naukoteka.ui.call.overlay.CallOverlayFragment
import uddug.com.naukoteka.ui.theme.NaukotekaTheme
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SingleCallFragment : Fragment() {

    private val viewModel: CallViewModel by activityViewModels()
    private var hasHandledCallFinish: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val contactName = arguments?.getString(ARG_CONTACT_NAME)
        val avatarUrl = arguments?.getString(ARG_AVATAR_URL)
        val callTitle = arguments?.getString(ARG_CALL_TITLE)
        val participants = arguments?.getParcelableArrayList<CallParticipant>(ARG_PARTICIPANTS)
        val dialogId = arguments?.getLong(ARG_DIALOG_ID)
        val isVideoCall = arguments?.getBoolean(ARG_IS_VIDEO_CALL) ?: true
        val isIncomingCall = arguments?.getBoolean(ARG_IS_INCOMING_CALL) ?: false
        val resolvedDialogId = dialogId ?: viewModel.uiState.value.dialogId ?: 0L

        if (isIncomingCall) {
            viewModel.showIncomingCall(
                dialogId = resolvedDialogId,
                contactName = contactName,
                avatarUrl = avatarUrl,
                participants = participants,
                callTitle = callTitle,
                isVideoCall = isVideoCall,
            )
        } else {
            viewModel.startCall(
                dialogId = resolvedDialogId,
                contactName = contactName,
                avatarUrl = avatarUrl,
                participants = participants,
                callTitle = callTitle,
                isVideoCall = isVideoCall,
            )
        }

        return ComposeView(requireContext()).apply {
            setContent {
                val state = viewModel.uiState.collectAsState().value
                NaukotekaTheme {
                    CallScreen(
                        state = state,
                        onBackPressed = { findNavController().popBackStack() },
                        onEndCall = {
                            viewModel.endCall()
                        },
                        onAcceptCall = {
                            viewModel.startCall(
                                dialogId = resolvedDialogId,
                                contactName = contactName,
                                avatarUrl = avatarUrl,
                                participants = participants,
                                callTitle = callTitle,
                                isVideoCall = isVideoCall,
                                isAcceptingIncomingCall = isIncomingCall,
                            )
                        },
                        onDeclineCall = viewModel::endCall,
                        onToggleMicrophone = viewModel::toggleMicrophone,
                        onToggleCamera = viewModel::toggleCamera,
                        onMinimize = {
                            if (!((requireActivity() as? ContainerActivity)?.enterCallPictureInPictureMode() ?: false)) {
                                showFloatingCall()
                                findNavController().popBackStack()
                            }
                        },
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeCallState()
    }

    private fun showFloatingCall() {
        val fragmentManager = requireActivity().supportFragmentManager
        if (fragmentManager.findFragmentByTag(CallOverlayFragment.TAG) == null) {
            fragmentManager.beginTransaction()
                .setReorderingAllowed(true)
                .add(android.R.id.content, CallOverlayFragment(), CallOverlayFragment.TAG)
                .commitNowAllowingStateLoss()
        }
    }

    private fun observeCallState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    if (state.status == CallStatus.FINISHED) {
                        handleCallFinished()
                    } else {
                        hasHandledCallFinish = false
                    }
                }
            }
        }
    }

    private fun handleCallFinished() {
        if (hasHandledCallFinish) return

        hasHandledCallFinish = true
        viewModel.endCall()
        removeFloatingCall()
        findNavController().popBackStack()
    }

    private fun removeFloatingCall() {
        requireActivity().supportFragmentManager.findFragmentByTag(CallOverlayFragment.TAG)?.let {
            requireActivity().supportFragmentManager.beginTransaction()
                .remove(it)
                .commitAllowingStateLoss()
        }
    }

    companion object {
        const val ARG_CONTACT_NAME = "contact_name"
        const val ARG_AVATAR_URL = "avatar_url"
        const val ARG_CALL_TITLE = "call_title"
        const val ARG_PARTICIPANTS = "participants"
        const val ARG_DIALOG_ID = "dialog_id"
        const val ARG_IS_VIDEO_CALL = "is_video_call"
        const val ARG_IS_INCOMING_CALL = "is_incoming_call"
    }
}
