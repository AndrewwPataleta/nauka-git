package uddug.com.naukoteka.ui.chat

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.core.content.ContextCompat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import uddug.com.domain.entities.chat.DialogInfo
import uddug.com.naukoteka.mvvm.chat.ChatDialogDetailViewModel
import uddug.com.naukoteka.mvvm.chat.ChatDetailUiState
import uddug.com.naukoteka.mvvm.chat.ChatDialogDetailEvent
import uddug.com.naukoteka.R
import uddug.com.naukoteka.presentation.profile.navigation.ContainerNavigationView
import uddug.com.naukoteka.ui.chat.compose.ChatDetailDialogComponent
import uddug.com.naukoteka.ui.chat.ChatAvatarPreviewFragment.Companion.ARG_AVATAR_PATH
import uddug.com.naukoteka.ui.chat.ChatEditGroupFragment
import uddug.com.naukoteka.ui.call.SingleCallFragment
import uddug.com.naukoteka.ui.theme.NaukotekaTheme
@AndroidEntryPoint
class ChatDetailDialogFragment : Fragment() {

    private var navigationView: ContainerNavigationView? = null

    private val viewModel: ChatDialogDetailViewModel by viewModels()

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
        lifecycleScope.launch {
            viewModel.events.collectLatest { event ->
                when (event) {
                    is ChatDialogDetailEvent.Share -> {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, event.link)
                        }
                        startActivity(Intent.createChooser(shareIntent, getString(R.string.share)))
                    }
                }
            }
        }

        findNavController().currentBackStackEntry?.savedStateHandle
            ?.getLiveData<Boolean>("refreshDialogInfo")
            ?.observe(viewLifecycleOwner) { shouldRefresh ->
                if (shouldRefresh == true) {
                    val dialogId = (viewModel.uiState.value as? ChatDetailUiState.Success)?.dialogId
                        ?: arguments?.getLong(DIALOG_ID)
                        ?: return@observe
                    viewModel.loadDialogInfo(dialogId)
                    findNavController().currentBackStackEntry?.savedStateHandle?.set("refreshDialogInfo", false)
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
                    ChatDetailDialogComponent(
                        viewModel = viewModel,
                        onBackPressed = {
                            requireActivity().onBackPressed()
                        },
                        onNavigateToProfile = {
                            viewModel.getCurrentUser()?.let { navigationView?.selectShowEditFragment(it) }
                        },
                        onSearchClick = {
                            val dialogId = (viewModel.uiState.value as? ChatDetailUiState.Success)?.dialogId
                                ?: arguments?.getLong(DIALOG_ID)
                                ?: 0L
                            findNavController().navigate(
                                R.id.chatDetailSearchFragment,
                                Bundle().apply { putLong(DIALOG_ID, dialogId) }
                            )
                        },
                        onCallClick = { name, avatar, isVideoCall ->
                            val dialogId = (viewModel.uiState.value as? ChatDetailUiState.Success)?.dialogId
                                ?: arguments?.getLong(DIALOG_ID)
                                ?: return@ChatDetailDialogComponent
                            ensureCallPermissions(
                                PendingCallRequest(
                                    name = name,
                                    avatar = avatar,
                                    dialogId = dialogId,
                                    isVideoCall = isVideoCall,
                                )
                            )
                        },
                        onChatDeleted = {
                            findNavController().popBackStack(R.id.chatListFragment, false)
                        },
                        onViewAvatar = { avatarPath ->
                            findNavController().navigate(
                                R.id.chatAvatarPreviewFragment,
                                Bundle().apply {
                                    putString(ARG_AVATAR_PATH, avatarPath)
                                }
                            )
                        },
                        onEditGroup = { id ->
                            val args = Bundle().apply {
                                putLong(ChatEditGroupFragment.ARG_DIALOG_ID, id)
                            }
                            findNavController().navigate(R.id.chatEditGroupFragment, args)
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
            R.id.singleCallFragment,
            Bundle().apply {
                putString(SingleCallFragment.ARG_CONTACT_NAME, request.name)
                putString(SingleCallFragment.ARG_AVATAR_URL, request.avatar)
                putLong(SingleCallFragment.ARG_DIALOG_ID, request.dialogId)
                putBoolean(SingleCallFragment.ARG_IS_VIDEO_CALL, request.isVideoCall)
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