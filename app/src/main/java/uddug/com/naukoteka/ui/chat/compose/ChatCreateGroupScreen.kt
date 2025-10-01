package uddug.com.naukoteka.ui.chat.compose

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Add

import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import uddug.com.naukoteka.BuildConfig
import uddug.com.naukoteka.R
import uddug.com.naukoteka.mvvm.chat.ChatCreateGroupUiState
import uddug.com.naukoteka.mvvm.chat.ChatCreateGroupViewModel
import uddug.com.naukoteka.mvvm.chat.GroupMember
import uddug.com.naukoteka.ui.chat.compose.components.Avatar
import uddug.com.naukoteka.ui.chat.compose.util.uriToFile

@Composable
fun ChatCreateGroupScreen(
    viewModel: ChatCreateGroupViewModel,
    onBackPressed: () -> Unit,
    onAddParticipantsClick: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val file = uriToFile(context, uri)
            if (file != null) {
                viewModel.onAvatarSelected(file)
            } else {
                Toast.makeText(
                    context,
                    R.string.chat_create_group_image_upload_error,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    val confirmEnabled = (uiState as? ChatCreateGroupUiState.Success)?.let { state ->
        state.members.count { !it.isCreator } >= 2 && !state.isSaving && !state.isAvatarUploading
    } ?: false

    var selectedMemberId by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.chat_create_group_title),
                        fontSize = 20.sp,
                        color = Color.Black
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_chat_back),
                            contentDescription = null,
                            tint = Color(0xFF2E83D9)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.onCreateGroupClick() },
                        enabled = confirmEnabled
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_chat_create_apply),
                            contentDescription = null,
                            tint = if (confirmEnabled) Color(0xFF2E83D9) else Color(0x4D2E83D9)
                        )
                    }
                },
                backgroundColor = Color.White,
                elevation = 0.dp
            )
        },
        backgroundColor = Color.White
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            when (val state = uiState) {
                ChatCreateGroupUiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is ChatCreateGroupUiState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = state.message, color = Color.Red)
                    }
                }

                is ChatCreateGroupUiState.Success -> {
                    LaunchedEffect(state.members) {
                        selectedMemberId?.let { id ->
                            if (state.members.none { it.user.id == id }) {
                                selectedMemberId = null
                            }
                        }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp)
                    ) {
                        item {
                            GroupHeader(
                                state = state,
                                onAvatarClick = { imagePickerLauncher.launch("image/*") },
                                onNameChanged = viewModel::onGroupNameChanged,
                                onAddParticipantsClick = onAddParticipantsClick
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = stringResource(
                                    R.string.chat_create_group_members,
                                    state.members.size
                                ),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp,
                                color = Color.Black
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        itemsIndexed(state.members) { index, member ->
                            GroupMemberRow(
                                member = member,
                                onMoreClick = { selectedMemberId = it.user.id }
                            )
                            if (index < state.members.lastIndex) {
                                Divider(color = Color(0xFFEAEAF2))
                            }
                        }
                    }

                    state.members.firstOrNull { it.user.id == selectedMemberId }?.let { member ->
                        GroupMemberActionsBottomSheet(
                            member = member,
                            onDismissRequest = { selectedMemberId = null },
                            onGrantAdminClick = {
                                member.user.id?.let { viewModel.onGrantAdminRights(it) }
                            },
                            onRevokeAdminClick = {
                                member.user.id?.let { viewModel.onRevokeAdminRights(it) }
                            },
                            onRemoveClick = {
                                member.user.id?.let { viewModel.onRemoveMember(it) }
                            }
                        )
                    }

                    if (state.isSaving) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.White.copy(alpha = 0.6f)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
    }

}

@Composable
private fun GroupHeader(
    state: ChatCreateGroupUiState.Success,
    onAvatarClick: () -> Unit,
    onNameChanged: (String) -> Unit,
    onAddParticipantsClick: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            GroupAvatarPicker(
                avatarPath = state.avatarPath,
                isUploading = state.isAvatarUploading,
                onClick = onAvatarClick
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                OutlinedTextField(
                    value = state.groupName,
                    onValueChange = onNameChanged,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = stringResource(R.string.chat_create_group_name_placeholder),
                            color = Color(0xFFB0B2C3)
                        )
                    },
                    singleLine = true,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        backgroundColor = Color.White,
                        focusedBorderColor = Color(0xFF2E83D9),
                        unfocusedBorderColor = Color(0xFFE0E0E8),
                        cursorColor = Color(0xFF2E83D9),
                        textColor = Color.Black
                    )
                )
                Text(
                    text = stringResource(
                        R.string.chat_create_group_name_counter,
                        state.groupName.length
                    ),
                    fontSize = 12.sp,
                    color = Color(0xFF8083A0),
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable { onAddParticipantsClick() }
                .background(Color(0xFFF5F5F9))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = null,
                tint = Color(0xFF2E83D9)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = stringResource(R.string.chat_create_group_add_participant),
                color = Color(0xFF2E83D9),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun GroupAvatarPicker(
    avatarPath: String?,
    isUploading: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFF5F5F9))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (!avatarPath.isNullOrEmpty()) {
            AsyncImage(
                model = BuildConfig.IMAGE_SERVER_URL + avatarPath,
                contentDescription = stringResource(R.string.chat_create_group_avatar_description),
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = stringResource(R.string.chat_create_group_avatar_description),
                tint = Color(0xFF2E83D9),
                modifier = Modifier.size(32.dp)
            )
        }

        if (isUploading) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.White.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            }
        }
    }
}

@Composable
private fun GroupMemberRow(
    member: GroupMember,
    onMoreClick: (GroupMember) -> Unit,
) {
    val canShowActions = !member.isCreator && member.user.id != null

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Avatar(
            url = member.user.image?.path,
            name = member.user.fullName,
            size = 40.dp
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = member.user.fullName.orEmpty(),
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                color = Color.Black
            )
            if (member.isAdmin && !member.isCreator) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.chat_create_group_admin_label),
                    color = Color(0xFF2E83D9),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                )
            }
            member.status?.takeIf { it.isNotBlank() }?.let { status ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = status,
                    fontSize = 12.sp,
                    color = Color(0xFF8083A0)
                )
            }
        }
        if (canShowActions) {
            IconButton(onClick = { onMoreClick(member) }) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = null,
                    tint = Color(0xFFB0B2C3)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GroupMemberActionsBottomSheet(
    member: GroupMember,
    onDismissRequest: () -> Unit,
    onGrantAdminClick: () -> Unit,
    onRevokeAdminClick: () -> Unit,
    onRemoveClick: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Text(
                text = stringResource(R.string.chat_create_group_member_actions_title),
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (!member.isCreator) {
                if (member.isAdmin) {
                    GroupMemberActionRow(
                        icon = Icons.Outlined.Delete,
                        text = stringResource(R.string.chat_create_group_member_revoke_admin),
                        onClick = {
                            onRevokeAdminClick()
                            onDismissRequest()
                        }
                    )
                } else {
                    GroupMemberActionRow(
                        icon = Icons.Outlined.Favorite,
                        text = stringResource(R.string.chat_create_group_member_give_admin),
                        onClick = {
                            onGrantAdminClick()
                            onDismissRequest()
                        }
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            GroupMemberActionRow(
                icon = Icons.Outlined.Delete,
                text = stringResource(R.string.chat_create_group_member_remove),
                textColor = Color(0xFFFF3B30),
                onClick = {
                    onRemoveClick()
                    onDismissRequest()
                }
            )
        }
    }
}

@Composable
private fun GroupMemberActionRow(
    icon: ImageVector,
    text: String,
    textColor: Color = Color.Black,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = textColor
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
    }
}
