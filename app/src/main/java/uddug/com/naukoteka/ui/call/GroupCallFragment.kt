package uddug.com.naukoteka.ui.call

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.compose.runtime.collectAsState
import androidx.core.content.ContextCompat
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import uddug.com.naukoteka.R
import uddug.com.naukoteka.mvvm.call.CallStatus
import uddug.com.naukoteka.mvvm.call.CallViewModel
import uddug.com.naukoteka.ui.activities.main.ContainerActivity
import uddug.com.naukoteka.ui.call.compose.CallScreen
import uddug.com.naukoteka.ui.call.overlay.CallOverlayFragment
import uddug.com.naukoteka.ui.theme.NaukotekaTheme

@AndroidEntryPoint
class GroupCallFragment : Fragment() {

    private val viewModel: CallViewModel by activityViewModels()
    private var hasHandledCallFinish: Boolean = false
    private var pendingStartCallRequest: PendingStartCallRequest? = null

    private val callPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val request = pendingStartCallRequest ?: return@registerForActivityResult
            pendingStartCallRequest = null

            val hasMicrophonePermission = permissions[Manifest.permission.RECORD_AUDIO] == true ||
                isPermissionGranted(Manifest.permission.RECORD_AUDIO)
            val hasCameraPermission = !request.isVideoCall ||
                permissions[Manifest.permission.CAMERA] == true ||
                isPermissionGranted(Manifest.permission.CAMERA)

            if (!hasMicrophonePermission) {
                showMicrophonePermissionAlert()
                return@registerForActivityResult
            }

            if (!hasCameraPermission) {
                showCameraPermissionAlert()
                return@registerForActivityResult
            }

            startCall(request)
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val contactName = arguments?.getString(ARG_CONTACT_NAME)
        val avatarUrl = arguments?.getString(ARG_AVATAR_URL)
        val dialogId = arguments?.getLong(ARG_DIALOG_ID)
        val isVideoCall = arguments?.getBoolean(ARG_IS_VIDEO_CALL) ?: true
        val resolvedDialogId = dialogId ?: viewModel.uiState.value.dialogId ?: 0L

        ensureCallPermissions(
            dialogId = resolvedDialogId,
            contactName = contactName,
            avatarUrl = avatarUrl,
            callTitle = contactName,
            isVideoCall = isVideoCall,
        )

        return ComposeView(requireContext()).apply {
            setContent {
                val state = viewModel.uiState.collectAsState().value
                NaukotekaTheme {
                    CallScreen(
                        state = state,
                        onBackPressed = { findNavController().popBackStack() },
                        onEndCall = { viewModel.endCall() },
                        onAcceptCall = {},
                        onDeclineCall = viewModel::endCall,
                        onToggleMicrophone = viewModel::toggleMicrophone,
                        onToggleCamera = viewModel::toggleCamera,
                        onToggleRecording = viewModel::toggleRecording,
                        onMinimize = {
                            if (!((requireActivity() as? ContainerActivity)?.enterCallPictureInPictureMode() ?: false)) {
                                showFloatingCall()
                                navigateBackToChatList()
                            }
                        },
                        onRemoteRendererReady = viewModel::bindRemoteRenderer,
                        onRemoteRendererReleased = { viewModel.clearRemoteRenderer() },
                        onBindLocalRenderer = viewModel::bindLocalRenderer,
                        onBindRemoteRenderer = viewModel::bindRemoteRenderer,
                        onReleaseLocalRenderer = viewModel::clearLocalRenderer,
                        onReleaseRemoteRenderer = viewModel::clearRemoteRenderer,
                        clearRemoteRenderer = viewModel::clearRemoteRenderer,
                        onMicPermissionDenied = viewModel::onMicPermissionDenied,
                        onAudioFocusFailed = viewModel::onAudioFocusFailed,
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeCallState()
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

    private fun navigateBackToChatList() {
        val navController = requireActivity().findNavController(R.id.main_nav_host_fragment)
        val popped = navController.popBackStack(R.id.chatListFragment, false)
        if (!popped) {
            navController.navigate(R.id.chatListFragment)
        }
    }

    private fun ensureCallPermissions(
        dialogId: Long,
        contactName: String?,
        avatarUrl: String?,
        callTitle: String?,
        isVideoCall: Boolean,
    ) {
        val missingPermissions = buildList {
            if (!isPermissionGranted(Manifest.permission.RECORD_AUDIO)) {
                add(Manifest.permission.RECORD_AUDIO)
            }
            if (isVideoCall && !isPermissionGranted(Manifest.permission.CAMERA)) {
                add(Manifest.permission.CAMERA)
            }
        }

        val request = PendingStartCallRequest(
            dialogId = dialogId,
            contactName = contactName,
            avatarUrl = avatarUrl,
            callTitle = callTitle,
            isVideoCall = isVideoCall,
        )

        if (missingPermissions.isEmpty()) {
            startCall(request)
            return
        }

        pendingStartCallRequest = request
        callPermissionLauncher.launch(missingPermissions.toTypedArray())
    }

    private fun startCall(request: PendingStartCallRequest) {
        viewModel.startCall(
            dialogId = request.dialogId,
            contactName = request.contactName,
            avatarUrl = request.avatarUrl,
            callTitle = request.callTitle,
            isVideoCall = request.isVideoCall,
        )
    }

    private fun showMicrophonePermissionAlert() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.call_permission_microphone_title)
            .setMessage(R.string.call_permission_microphone_message)
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    private fun showCameraPermissionAlert() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.call_permission_camera_title)
            .setMessage(R.string.call_permission_camera_message)
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    private fun isPermissionGranted(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(requireContext(), permission) ==
            PackageManager.PERMISSION_GRANTED
    }

    companion object {
        const val ARG_CONTACT_NAME = "contact_name"
        const val ARG_AVATAR_URL = "avatar_url"
        const val ARG_DIALOG_ID = "dialog_id"
        const val ARG_IS_VIDEO_CALL = "is_video_call"
    }

    private data class PendingStartCallRequest(
        val dialogId: Long,
        val contactName: String?,
        val avatarUrl: String?,
        val callTitle: String?,
        val isVideoCall: Boolean,
    )
}
