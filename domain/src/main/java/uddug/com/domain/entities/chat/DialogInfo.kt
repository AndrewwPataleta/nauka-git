package uddug.com.domain.entities.chat

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize


@Parcelize
data class DialogInfo(
    val id: Long,
    val name: String?,
    val type: Int,
    val interlocutor: User?,
    val dialogImage: File?,
    val users: List<User>? = null,
    val isDeleted: Boolean,
    val firstMessageId: Long?,
    val isPinned: Boolean,
    val isUnread: Boolean,
    val pinnedMessageId: Long?,
    val activeCall: ActiveCall?,
    val permits: List<String>,
): Parcelable

@Parcelize
data class File(
    val id: String,
    val path: String,
    val fileName: String? = null,
    val contentType: String? = null,
    val fileSize: Int? = null,
    val fileType: Int? = null,
    val fileKind: Int? = null,
    val duration: String? = null,
    val viewCount: Int? = null,
): Parcelable
@Parcelize
data class ActiveCall(
    val id: Long,
    val format: Int,
    val type: Int,
): Parcelable
