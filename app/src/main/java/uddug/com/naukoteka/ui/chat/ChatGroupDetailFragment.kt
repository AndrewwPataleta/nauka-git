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
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import uddug.com.domain.entities.chat.DialogInfo
import uddug.com.naukoteka.R
import uddug.com.naukoteka.mvvm.chat.ChatGroupDetailViewModel
import uddug.com.naukoteka.mvvm.chat.ChatGroupDetailUiState
import uddug.com.naukoteka.presentation.profile.navigation.ContainerNavigationView
import uddug.com.naukoteka.ui.chat.compose.ChatGroupDetailComponent
import uddug.com.naukoteka.ui.chat.ChatAvatarPreviewFragment.Companion.ARG_AVATAR_PATH
import uddug.com.naukoteka.ui.call.GroupCallFragment
import uddug.com.naukoteka.ui.theme.NaukotekaTheme

@AndroidEntryPoint
class ChatGroupDetailFragment : Fragment() {

    private var navigationView: ContainerNavigationView? = null

    private val viewModel: ChatGroupDetailViewModel by viewModels()

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
    }

    companion object {
        const val DIALOG_ID = "DIALOG_ID"
        const val DIALOG_DETAIL = "DIALOG_DETAIL"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                when (state) {
                    else -> {}
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        arguments?.getParcelable<DialogInfo>(DIALOG_DETAIL)
            ?.let { viewModel.setDialogInfo(it) }
            ?: arguments?.getLong(DIALOG_ID)?.let { viewModel.loadDialogInfo(it) }

        return ComposeView(requireContext()).apply {
            setContent {
                NaukotekaTheme {
                    ChatGroupDetailComponent(
                        viewModel = viewModel,
                        onBackPressed = { requireActivity().onBackPressed() },
                        onSearchClick = {
                            val dialogId = (viewModel.uiState.value as? ChatGroupDetailUiState.Success)?.dialogId ?: 0L
                            findNavController().navigate(
                                R.id.chatDetailSearchFragment,
                                Bundle().apply { putLong(ChatDetailDialogFragment.DIALOG_ID, dialogId) }
                            )
                        },
                        onAddParticipantsClick = {
                            findNavController().navigate(R.id.chatCreateMultiFragment)
                        },
                        onCallClick = { name, avatar, isVideoCall ->
                            val dialogId = (viewModel.uiState.value as? ChatGroupDetailUiState.Success)?.dialogId
                                ?: return@ChatGroupDetailComponent
                            ensureCallPermissions(
                                PendingCallRequest(
                                    name = name,
                                    avatar = avatar,
                                    dialogId = dialogId,
                                    isVideoCall = isVideoCall,
                                )
                            )
                        },
                        onViewAvatar = { avatarPath ->
                            findNavController().navigate(
                                R.id.chatAvatarPreviewFragment,
                                Bundle().apply { putString(ARG_AVATAR_PATH, avatarPath) }
                            )
                        },
                        onUserClick = { userId ->
                            if (userId.isNotBlank()) {
                                findNavController().navigate(
                                    R.id.otherProfileFragment,
                                    Bundle().apply { putString("userId", userId) }
                                )
                            }
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
        findNavController().navigate(
            R.id.groupCallFragment,
            Bundle().apply {
                putString(GroupCallFragment.ARG_CONTACT_NAME, request.name)
                putString(GroupCallFragment.ARG_AVATAR_URL, request.avatar.orEmpty())
                putLong(GroupCallFragment.ARG_DIALOG_ID, request.dialogId)
                putBoolean(GroupCallFragment.ARG_IS_VIDEO_CALL, request.isVideoCall)
            }
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

    private data class PendingCallRequest(
        val name: String?,
        val avatar: String?,
        val dialogId: Long,
        val isVideoCall: Boolean,
    )
}
