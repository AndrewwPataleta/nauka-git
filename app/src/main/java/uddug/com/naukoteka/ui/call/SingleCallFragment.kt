package uddug.com.naukoteka.ui.call

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.Context
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.compose.runtime.collectAsState
import androidx.core.content.ContextCompat
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.findNavController
import dagger.hilt.android.AndroidEntryPoint
import uddug.com.naukoteka.mvvm.call.CallParticipant
import uddug.com.naukoteka.mvvm.call.CallStatus
import uddug.com.naukoteka.mvvm.call.CallViewModel
import uddug.com.naukoteka.mvvm.call.ScreenShareEvent
import uddug.com.naukoteka.services.ScreenShareBridge
import uddug.com.naukoteka.services.ScreenShareService
import uddug.com.naukoteka.presentation.profile.navigation.ContainerNavigationView
import uddug.com.naukoteka.ui.call.compose.CallScreen
import uddug.com.naukoteka.ui.chat.ChatDialogFragment
import uddug.com.naukoteka.ui.call.overlay.CallOverlayFragment
import uddug.com.naukoteka.ui.theme.NaukotekaTheme
import kotlinx.coroutines.launch
import uddug.com.naukoteka.R

@AndroidEntryPoint
class SingleCallFragment : Fragment() {

    private val viewModel: CallViewModel by activityViewModels()
    private var navigationView: ContainerNavigationView? = null
    private var hasHandledCallFinish: Boolean = false
    private var pendingStartCallRequest: PendingStartCallRequest? = null

    // Токен разрешения MediaProjection: саму проекцию строим только после подъёма
    // foreground-сервиса (требование Android 14). См. GroupCallFragment.
    private var pendingProjectionData: Intent? = null

    private val screenCaptureLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                pendingProjectionData = result.data
                ScreenShareBridge.onForegrounded = { startScreenCaptureFromProjection() }
                ScreenShareBridge.onStopRequested = { viewModel.stopScreenShare() }
                ScreenShareService.start(requireContext())
            } else {
                Toast.makeText(requireContext(), "Демонстрация экрана отменена", Toast.LENGTH_SHORT).show()
            }
        }

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

    override fun onAttach(context: Context) {
        super.onAttach(context)
        navigationView = requireActivity() as? ContainerNavigationView
    }

    override fun onResume() {
        super.onResume()
        navigationView?.showNavigationBottomBar(false)
        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        viewModel.ensureSpeakerphoneOn()
        viewModel.onAppResumed()
    }

    override fun onPause() {
        super.onPause()
        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

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
        // Default to audio: never enable the camera unless the call is
        // explicitly a video call (see incoming-call isVideoCall threading).
        val isVideoCall = arguments?.getBoolean(ARG_IS_VIDEO_CALL) ?: false
        val isIncomingCall = arguments?.getBoolean(ARG_IS_INCOMING_CALL) ?: false
        val isGroupCall = arguments?.getBoolean(ARG_IS_GROUP_CALL) ?: false
        val resolvedDialogId = dialogId ?: viewModel.uiState.value.dialogId ?: 0L

        if (isIncomingCall) {
            viewModel.showIncomingCall(
                dialogId = resolvedDialogId,
                contactName = contactName,
                avatarUrl = avatarUrl,
                participants = participants,
                callTitle = callTitle,
                isVideoCall = isVideoCall,
                isGroupCall = isGroupCall,
            )
        } else {
            ensureCallPermissions(
                dialogId = resolvedDialogId,
                contactName = contactName,
                avatarUrl = avatarUrl,
                participants = participants,
                callTitle = callTitle,
                isVideoCall = isVideoCall,
                isAcceptingIncomingCall = false,
                isGroupCall = isGroupCall,
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
                            ensureCallPermissions(
                                dialogId = resolvedDialogId,
                                contactName = contactName,
                                avatarUrl = avatarUrl,
                                participants = participants,
                                callTitle = callTitle,
                                isVideoCall = isVideoCall,
                                isAcceptingIncomingCall = isIncomingCall,
                                isGroupCall = isGroupCall,
                            )
                        },
                        onDeclineCall = viewModel::endCall,
                        onToggleMicrophone = viewModel::toggleMicrophone,
                        onToggleCamera = viewModel::toggleCamera,
                        onToggleRecording = viewModel::toggleRecording,
                        onStartRecording = viewModel::startRecording,
                        onMinimize = {
                            showFloatingCall()
                            navigateBackToChatList()
                        },
                        onRemoteRendererReady = viewModel::bindRemoteRenderer,
                        onRemoteRendererReleased = { viewModel.clearRemoteRenderer() },
                        onBindLocalRenderer = viewModel::bindLocalRenderer,
                        onBindRemoteRenderer = viewModel::bindRemoteRenderer,
                        onReleaseLocalRenderer = viewModel::clearLocalRenderer,
                        onReleaseRemoteRenderer = { viewModel.clearRemoteRenderer() },
                        clearRemoteRenderer = { viewModel.clearRemoteRenderer() },
                        onBindParticipantRenderer = { participantId, renderer ->
                            viewModel.bindParticipantRenderer(participantId, renderer)
                        },
                        onReleaseParticipantRenderer = { participantId ->
                            viewModel.releaseParticipantRenderer(participantId)
                        },
                        onMuteParticipant = { viewModel.muteParticipant(it) },
                        onToastConsumed = viewModel::consumeToast,
                        onMicPermissionDenied = viewModel::onMicPermissionDenied,
                        onAudioFocusFailed = viewModel::onAudioFocusFailed,
                        onSwitchCamera = viewModel::switchCamera,
                        onToggleHand = viewModel::toggleHandRaise,
                        onSelectAudioRoute = viewModel::selectAudioRoute,
                        onSetParticipantPermit = viewModel::setParticipantPermit,
                        onSetCallVolume = viewModel::setCallVolume,
                        getCallVolume = viewModel::currentCallVolume,
                        onShareLink = { shareCallLink() },
                        onToggleScreenShare = { viewModel.toggleScreenShare() },
                        onLeaveCall = { viewModel.endCall() },
                        onEndForAll = { viewModel.endCallForEveryone() },
                        onBindPreviewRenderer = viewModel::bindPreviewRenderer,
                        onReleasePreviewRenderer = viewModel::releasePreviewRenderer,
                        onConfirmPreJoin = viewModel::confirmPreJoin,
                        onCancelPreJoin = viewModel::abortPreJoin,
                        onFlipPreviewCamera = viewModel::flipPreviewCamera,
                        onOpenParticipantProfile = { userId -> openOtherProfile(userId) },
                    )
                }
            }
        }
    }

    // Тап по участнику звонка открывает его «Чужой профиль» (глобальный пункт
    // nav-графа). Пустой id игнорируем; тап по себе обрабатывает сам экран.
    private fun openOtherProfile(userId: String) {
        if (userId.isBlank()) return
        runCatching {
            findNavController().navigate(
                R.id.otherProfileFragment,
                Bundle().apply { putString("userId", userId) },
            )
        }
    }

    private fun shareCallLink() {
        val title = viewModel.uiState.value.callTitle
        val text = buildString {
            append("Присоединяйтесь к звонку в Наукотеке")
            if (!title.isNullOrBlank()) append(": $title")
        }
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        runCatching {
            startActivity(Intent.createChooser(shareIntent, getString(R.string.share)))
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeCallState()
        observeToasts()
        observeScreenShareEvents()
    }

    private fun observeScreenShareEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.screenShareEvents.collect { event ->
                    when (event) {
                        ScreenShareEvent.RequestPermission -> requestScreenCapture()
                        ScreenShareEvent.StopService -> stopScreenShareService()
                    }
                }
            }
        }
    }

    private fun requestScreenCapture() {
        val mpm = requireContext()
            .getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        runCatching { screenCaptureLauncher.launch(mpm.createScreenCaptureIntent()) }
            .onFailure {
                Toast.makeText(
                    requireContext(),
                    "Демонстрация экрана недоступна на этом устройстве",
                    Toast.LENGTH_SHORT,
                ).show()
            }
    }

    private fun startScreenCaptureFromProjection() {
        val data = pendingProjectionData ?: return
        val mpm = requireContext()
            .getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        val projection = runCatching { mpm.getMediaProjection(Activity.RESULT_OK, data) }.getOrNull()
        if (projection == null) {
            stopScreenShareService()
            return
        }
        viewModel.startScreenShare(projection, data)
    }

    private fun stopScreenShareService() {
        pendingProjectionData = null
        ScreenShareBridge.clear()
        runCatching { ScreenShareService.stop(requireContext()) }
    }

    private fun observeToasts() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.toastEvents.collect { message ->
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
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
        stopScreenShareService()
        viewModel.endCall()
        removeFloatingCall()
        // После завершения звонка открываем ЧАТ, где шёл звонок, а не список.
        navigateBackToChatList()
    }

    // При сворачивании звонка возвращаемся в ленту чата, где идёт звонок, а не в
    // список диалогов. Если чат уже в бэкстеке — попаем к нему, иначе открываем
    // заново по dialogId. Всё в runCatching: findNavController может кинуть, если
    // хост не готов (краш при сворачивании).
    private fun navigateBackToChatList() {
        runCatching {
            val navController = requireActivity().findNavController(R.id.main_nav_host_fragment)
            if (navController.popBackStack(R.id.chatDialogFragment, false)) return@runCatching

            val dialogId = viewModel.uiState.value.dialogId
            if (dialogId != null && dialogId != 0L) {
                navController.popBackStack(R.id.chatListFragment, false)
                navController.navigate(
                    R.id.chatDialogFragment,
                    Bundle().apply { putLong(ChatDialogFragment.DIALOG_ID, dialogId) },
                )
            } else if (!navController.popBackStack(R.id.chatListFragment, false)) {
                navController.navigate(R.id.chatListFragment)
            }
        }
    }

    private fun removeFloatingCall() {
        requireActivity().supportFragmentManager.findFragmentByTag(CallOverlayFragment.TAG)?.let {
            requireActivity().supportFragmentManager.beginTransaction()
                .remove(it)
                .commitAllowingStateLoss()
        }
    }

    private fun ensureCallPermissions(
        dialogId: Long,
        contactName: String?,
        avatarUrl: String?,
        participants: ArrayList<CallParticipant>?,
        callTitle: String?,
        isVideoCall: Boolean,
        isAcceptingIncomingCall: Boolean,
        isGroupCall: Boolean,
    ) {
        val missingPermissions = buildList {
            if (!isPermissionGranted(Manifest.permission.RECORD_AUDIO)) {
                add(Manifest.permission.RECORD_AUDIO)
            }
            if (isVideoCall && !isPermissionGranted(Manifest.permission.CAMERA)) {
                add(Manifest.permission.CAMERA)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                !isPermissionGranted(Manifest.permission.BLUETOOTH_CONNECT)
            ) {
                add(Manifest.permission.BLUETOOTH_CONNECT)
            }
        }

        val request = PendingStartCallRequest(
            dialogId = dialogId,
            contactName = contactName,
            avatarUrl = avatarUrl,
            participants = participants,
            callTitle = callTitle,
            isVideoCall = isVideoCall,
            isAcceptingIncomingCall = isAcceptingIncomingCall,
            isGroupCall = isGroupCall,
        )

        if (missingPermissions.isEmpty()) {
            startCall(request)
            return
        }

        pendingStartCallRequest = request
        callPermissionLauncher.launch(missingPermissions.toTypedArray())
    }

    private fun startCall(request: PendingStartCallRequest) {
        // ВИДЕОзвонок (исходящий ИЛИ приём входящего) — сначала пре-экран настройки,
        // реальный вход/оповещение только по «Присоединиться». Аудио — как раньше.
        if (request.isVideoCall) {
            viewModel.enterPreJoin(
                dialogId = request.dialogId,
                contactName = request.contactName,
                avatarUrl = request.avatarUrl,
                callTitle = request.callTitle,
                isVideoCall = true,
                isGroupCall = request.isGroupCall,
                participants = request.participants,
                isAcceptingIncomingCall = request.isAcceptingIncomingCall,
            )
        } else {
            viewModel.startCall(
                dialogId = request.dialogId,
                contactName = request.contactName,
                avatarUrl = request.avatarUrl,
                participants = request.participants,
                callTitle = request.callTitle,
                isVideoCall = request.isVideoCall,
                isAcceptingIncomingCall = request.isAcceptingIncomingCall,
                isGroupCall = request.isGroupCall,
            )
        }
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
        const val ARG_CALL_TITLE = "call_title"
        const val ARG_PARTICIPANTS = "participants"
        const val ARG_DIALOG_ID = "dialog_id"
        const val ARG_IS_VIDEO_CALL = "is_video_call"
        const val ARG_IS_INCOMING_CALL = "is_incoming_call"
        const val ARG_IS_GROUP_CALL = "is_group_call"
    }

    private data class PendingStartCallRequest(
        val dialogId: Long,
        val contactName: String?,
        val avatarUrl: String?,
        val participants: ArrayList<CallParticipant>?,
        val callTitle: String?,
        val isVideoCall: Boolean,
        val isAcceptingIncomingCall: Boolean,
        val isGroupCall: Boolean,
    )
}
