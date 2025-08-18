package uddug.com.domain.entities.chat

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

// Chat.kt

data class Chat(
    val dialogId: Long,
    val dialogName: String,
    val dialogType: Int,
    val messageId: Long,
    val isPinned: Boolean,
    val isUnread: Boolean,
    val users: List<User>,
    val interlocutor: User,
    val lastMessage: Message,
    val unreadMessages: Int,
    val notificationsDisable: Boolean,
    val isBlocked: Boolean,
)

@Parcelize
data class User(
    val image: String? = null,
    val fullName: String? = null,
    val nickname: String? = null,
    val userId: String? = null,
    val role: String? = null,
) : Parcelable

data class Image(
    val path: String? = null,
)

data class Message(
    val id: Long? = null,
    val text: String? = null,
    val type: Int? = null,
    val files: List<Image>? = null,
    val read: Int? = null,
    val ownerId: String? = null,
    val createdAt: String? = null,
    val isPinned: Boolean? = null,
)

fun MessageChat.updateOwnerInfoFromDialog(dialogInfo: DialogInfo): MessageChat {

    val ownerUser = dialogInfo.users?.find { it.userId == this.ownerId }
        ?: if (dialogInfo.interlocutor?.userId == this.ownerId) dialogInfo.interlocutor
        else null


    val name = ownerUser?.let {
        it.fullName ?: it.nickname ?: ""
    } ?: ""

    val avatarUrl = ownerUser?.image ?: ""


    return this.copy(
        ownerName = name,
        ownerAvatarUrl = avatarUrl,
        ownerIsAdmin = ownerUser?.role == "admin"
    )
}