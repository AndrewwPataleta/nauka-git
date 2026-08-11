package uddug.com.naukoteka.ui.chat

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import uddug.com.domain.entities.chat.Chat
import uddug.com.domain.entities.profile.UserProfileFullInfo
import uddug.com.naukoteka.R
import uddug.com.naukoteka.mvvm.chat.ChatDialogEvents
import uddug.com.naukoteka.mvvm.chat.ChatDialogUiState
import uddug.com.naukoteka.mvvm.chat.ChatDialogViewModel
import uddug.com.naukoteka.mvvm.chat.ChatListEvents
import uddug.com.naukoteka.mvvm.chat.ChatListUiState
import uddug.com.naukoteka.mvvm.chat.ChatListViewModel
import uddug.com.naukoteka.presentation.profile.navigation.ContainerNavigationView
import uddug.com.naukoteka.ui.chat.ChatDetailDialogFragment.Companion.DIALOG_DETAIL
import uddug.com.naukoteka.ui.fragments.profile.ProfileFullInfoBottomSheetFragment
import uddug.com.naukoteka.ui.chat.ForwardMessageFragment.Companion.ARG_MESSAGE_ID
import uddug.com.naukoteka.ui.chat.ForwardMessageFragment.Companion.ARG_MESSAGE_IDS
import uddug.com.naukoteka.ui.chat.ForwardMessageFragment.Companion.ARG_FORWARD_TEXT
import uddug.com.naukoteka.ui.chat.ForwardMessageFragment.Companion.ARG_FORWARD_AUTHOR
import uddug.com.domain.entities.chat.MessageChat
import uddug.com.domain.entities.chat.MessageType
import uddug.com.naukoteka.ui.call.GroupCallFragment
import uddug.com.naukoteka.ui.call.SingleCallFragment
import uddug.com.naukoteka.ui.chat.compose.ChatDialogComponent
import uddug.com.naukoteka.services.IncomingCallSocketService
import uddug.com.naukoteka.ui.theme.NaukotekaTheme
import uddug.com.naukoteka.ui.chat.compose.ChatListComponent
import uddug.com.naukoteka.ui.chat.ChatEditGroupFragment
import uddug.com.naukoteka.ui.chat.ChatPollResultsFragment
import javax.inject.Inject
import uddug.com.naukoteka.utils.SharedContentStore

@AndroidEntryPoint
class ChatDialogFragment : Fragment() {

    private var navigationView: ContainerNavigationView? = null

    private val viewModel: ChatDialogViewModel by viewModels()

    @Inject
    lateinit var sharedContentStore: SharedContentStore

    private var dialogId: Long = 0
    private var hasConsumedSharedContent = false

    private var pendingCallRequest: PendingCallRequest? = null

    private val callPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val request = pendingCallRequest ?: return@registerForActivityResult
            pendingCallRequest = null

            val hasMicrophonePermission = permissions[Manifest.permission.RECORD_AUDIO] == true ||
                isPermissionGranted(Manifest.permission.RECORD_AUDIO)
            val hasCameraPermission = !request.isVideoCall || permissions[Manifest.permission.CAMERA] == true ||
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
        navigationView = requireActivity() as ContainerNavigationView
    }

    override fun onResume() {
        super.onResume()
        navigationView?.showNavigationBottomBar(false)
        dismissMessageNotification()
    }

    /**
     * Clears the new-message notification for this dialog (and with it the
     * launcher-icon badge) once the user is actually viewing the conversation.
     * The notification id mirrors IncomingCallSocketService.sendMessageNotification.
     */
    private fun dismissMessageNotification() {
        if (dialogId == 0L) return
        NotificationManagerCompat.from(requireContext())
            .cancel(IncomingCallSocketService.CHAT_NOTIFICATION_BASE_ID + dialogId.toInt())
    }

    companion object {
        const val DIALOG_ID = "DIALOG_ID"
        const val INTERLOCUTOR_ID = "INTERLOCUTOR_ID"
        const val CREATED_POLL_ID_KEY = "createdPollId"
        const val ARG_PENDING_FORWARD_IDS = "pending_forward_ids"
        const val ARG_PENDING_FORWARD_TEXT = "pending_forward_text"
        const val ARG_PENDING_FORWARD_AUTHOR = "pending_forward_author"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
        dialogId = arguments?.getLong(DIALOG_ID) ?: 0
        val peerId = arguments?.getString(INTERLOCUTOR_ID)
        if (dialogId != 0L) {
            viewModel.loadMessages(dialogId)
        } else if (!peerId.isNullOrEmpty()) {
            viewModel.loadMessagesByPeer(peerId)
        }
        consumeSharedContentIfAny()
        consumePendingForwardIfAny()
    }

    private fun consumePendingForwardIfAny() {
        val forwardIds = arguments?.getLongArray(ARG_PENDING_FORWARD_IDS) ?: return
        if (forwardIds.isEmpty()) return
        val text = arguments?.getString(ARG_PENDING_FORWARD_TEXT)
        val author = arguments?.getString(ARG_PENDING_FORWARD_AUTHOR)
        viewModel.setPendingForward(forwardIds.toList(), text, author)
        arguments?.remove(ARG_PENDING_FORWARD_IDS)
    }

    private fun consumeSharedContentIfAny() {
        if (hasConsumedSharedContent) return
        if (!sharedContentStore.hasPending()) return
        hasConsumedSharedContent = true
        val files = sharedContentStore.consumeAsFiles(requireContext())
        if (files.isNotEmpty()) {
            viewModel.attachFiles(files)
        }
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                when (state) {
                    else -> {}
                }
            }
        }
        lifecycleScope.launch {
            viewModel.events.collectLatest { state ->
                when (state) {
                    is ChatDialogEvents.OpenChatProfileDetail -> {
                        val isGroup = (state.dialogInfo.users?.size ?: 0) > 2
                        val destination = if (isGroup) {
                            R.id.chatGroupDetailFragment
                        } else {
                            R.id.chatDetailDialog
                        }
                        findNavController().navigate(
                            destination,
                            args = Bundle().apply {
                                putLong(DIALOG_ID, state.dialogId)
                                putParcelable(DIALOG_DETAIL, state.dialogInfo)
                            }
                        )
                    }

                    is ChatDialogEvents.OpenUserProfile -> {
                        ProfileFullInfoBottomSheetFragment
                            .newInstance(state.profile)
                            .show(childFragmentManager, "user_profile")
                    }
                }
            }
        }

        findNavController().currentBackStackEntry?.savedStateHandle
            ?.getLiveData<UserProfileFullInfo>("selectedUser")
            ?.observe(viewLifecycleOwner) { user ->
                user?.let { viewModel.attachUserContact(it) }
            }

        findNavController().currentBackStackEntry?.savedStateHandle
            ?.getLiveData<Boolean>("refreshDialogInfo")
            ?.observe(viewLifecycleOwner) { shouldRefresh ->
                if (shouldRefresh == true) {
                    viewModel.refreshDialogInfo()
                    findNavController().currentBackStackEntry?.savedStateHandle?.set("refreshDialogInfo", false)
                }
            }

        val savedStateHandle = findNavController().currentBackStackEntry?.savedStateHandle
        savedStateHandle
            ?.getLiveData<String>(CREATED_POLL_ID_KEY)
            ?.observe(viewLifecycleOwner) { pollId ->
                if (!pollId.isNullOrBlank()) {
                    viewModel.sendPoll(pollId)
                    savedStateHandle.remove<String>(CREATED_POLL_ID_KEY)
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        return ComposeView(requireContext()).apply {
            setContent {
                NaukotekaTheme {
                    ChatDialogComponent(
                        viewModel = viewModel,
                        onBackPressed = {
                            requireActivity().onBackPressed()
                        },
                        onCallClick = { name, avatar, isVideoCall ->
                            val callDialogId = viewModel.currentDialogId.value ?: dialogId
                            if (callDialogId != 0L) {
                                val isGroupCall = (viewModel.uiState.value as? ChatDialogUiState.Success)
                                    ?.isGroup ?: false
                                ensureCallPermissions(
                                    PendingCallRequest(
                                        name = name,
                                        avatar = avatar,
                                        dialogId = callDialogId,
                                        isVideoCall = isVideoCall,
                                        isGroupCall = isGroupCall,
                                    )
                                )
                            }
                        },
                        onContactClick = {
                            findNavController().navigate(R.id.sendContactFragment)
                        },
                        onCreatePoll = {
                            findNavController().navigate(R.id.chatCreatePollFragment)
                        },
                        onOpenPollResults = { pollId, poll ->
                            val args = Bundle().apply {
                                putString(ChatPollResultsFragment.ARG_POLL_ID, pollId)
                                if (poll != null) {
                                    putString(ChatPollResultsFragment.ARG_POLL_JSON, com.google.gson.Gson().toJson(poll))
                                }
                            }
                            findNavController().navigate(R.id.chatPollResultsFragment, args)
                        },
                        onForwardMessage = { message ->
                            val forwardText = message.text?.takeIf { it.isNotBlank() }
                                ?: describeMessageContent(message)
                            val args = Bundle().apply {
                                putLong(ARG_MESSAGE_ID, message.id)
                                putString(ARG_FORWARD_TEXT, forwardText)
                                putString(ARG_FORWARD_AUTHOR, message.ownerName)
                            }
                            findNavController().navigate(R.id.forwardMessageFragment, args)
                        },
                        onForwardSelected = { ids ->
                            val args = Bundle().apply {
                                putLongArray(ARG_MESSAGE_IDS, ids.toLongArray())
                            }
                            findNavController().navigate(R.id.forwardMessageFragment, args)
                        },
                        onEditGroup = { id ->
                            val args = Bundle().apply {
                                putLong(ChatEditGroupFragment.ARG_DIALOG_ID, id)
                            }
                            findNavController().navigate(R.id.chatEditGroupFragment, args)
                        },
                        onChatDeleted = {
                            findNavController().previousBackStackEntry?.savedStateHandle?.set("refreshChats", true)
                            findNavController().popBackStack()
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

    private fun ensureCallPermissions(request: PendingCallRequest) {
        val requiredPermissions = buildList {
            add(Manifest.permission.RECORD_AUDIO)
            if (request.isVideoCall) {
                add(Manifest.permission.CAMERA)
            }
        }

        val missingPermissions = requiredPermissions.filterNot(::isPermissionGranted)

        if (missingPermissions.isEmpty()) {
            startCall(request)
            return
        }

        pendingCallRequest = request
        callPermissionLauncher.launch(missingPermissions.toTypedArray())
    }

    private fun startCall(request: PendingCallRequest) {
        // Групповой чат — отдельный экран groupCallFragment (как из деталей
        // группы), личный — singleCallFragment.
        if (request.isGroupCall) {
            findNavController().navigate(
                R.id.groupCallFragment,
                Bundle().apply {
                    putString(GroupCallFragment.ARG_CONTACT_NAME, request.name)
                    putString(GroupCallFragment.ARG_AVATAR_URL, request.avatar.orEmpty())
                    putLong(GroupCallFragment.ARG_DIALOG_ID, request.dialogId)
                    putBoolean(GroupCallFragment.ARG_IS_VIDEO_CALL, request.isVideoCall)
                }
            )
        } else {
            findNavController().navigate(
                R.id.singleCallFragment,
                Bundle().apply {
                    putString(SingleCallFragment.ARG_CONTACT_NAME, request.name)
                    putString(SingleCallFragment.ARG_AVATAR_URL, request.avatar)
                    putLong(SingleCallFragment.ARG_DIALOG_ID, request.dialogId)
                    putBoolean(SingleCallFragment.ARG_IS_VIDEO_CALL, request.isVideoCall)
                    putBoolean(SingleCallFragment.ARG_IS_GROUP_CALL, request.isGroupCall)
                }
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

    private data class PendingCallRequest(
        val name: String?,
        val avatar: String?,
        val dialogId: Long,
        val isVideoCall: Boolean,
        val isGroupCall: Boolean,
    )

    private fun describeMessageContent(message: MessageChat): String? {
        if (message.type == MessageType.POLL) return getString(R.string.chat_poll_label)
        if (message.type == MessageType.VOICE) return getString(R.string.chat_voice_message)
        val file = message.files.firstOrNull() ?: return null
        val ct = file.contentType?.lowercase()
        return when {
            ct?.startsWith("image") == true -> getString(R.string.chat_last_message_image)
            ct?.startsWith("video") == true -> getString(R.string.chat_last_message_video)
            ct?.startsWith("audio") == true -> getString(R.string.chat_voice_message)
            file.fileType == 1 -> getString(R.string.chat_last_message_image)
            file.fileType == 30 -> getString(R.string.chat_last_message_video)
            file.fileType == 21 -> getString(R.string.chat_voice_message)
            file.fileType == 20 -> getString(R.string.chat_last_message_file)
            else -> getString(R.string.chat_last_message_file)
        }
    }
}
