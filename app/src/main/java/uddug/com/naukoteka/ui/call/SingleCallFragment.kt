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
import dagger.hilt.android.AndroidEntryPoint
import uddug.com.naukoteka.mvvm.call.CallParticipant
import uddug.com.naukoteka.mvvm.call.CallViewModel
import uddug.com.naukoteka.ui.activities.main.ContainerActivity
import uddug.com.naukoteka.ui.call.compose.CallScreen
import uddug.com.naukoteka.ui.call.overlay.CallOverlayFragment
import uddug.com.naukoteka.ui.theme.NaukotekaTheme

@AndroidEntryPoint
class SingleCallFragment : Fragment() {

    private val viewModel: CallViewModel by activityViewModels()

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

        viewModel.startCall(
            dialogId = dialogId ?: viewModel.uiState.value.dialogId ?: 0L,
            contactName = contactName,
            avatarUrl = avatarUrl,
            participants = participants,
            callTitle = callTitle,
            isVideoCall = isVideoCall,
        )

        return ComposeView(requireContext()).apply {
            setContent {
                val state = viewModel.uiState.collectAsState().value
                NaukotekaTheme {
                    CallScreen(
                        state = state,
                        onBackPressed = { findNavController().popBackStack() },
                        onEndCall = {
                            viewModel.endCall()
                            findNavController().popBackStack()
                            removeFloatingCall()
                        },
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

    private fun showFloatingCall() {
        val fragmentManager = requireActivity().supportFragmentManager
        if (fragmentManager.findFragmentByTag(CallOverlayFragment.TAG) == null) {
            fragmentManager.beginTransaction()
                .setReorderingAllowed(true)
                .add(android.R.id.content, CallOverlayFragment(), CallOverlayFragment.TAG)
                .commitNowAllowingStateLoss()
        }
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
    }
}
