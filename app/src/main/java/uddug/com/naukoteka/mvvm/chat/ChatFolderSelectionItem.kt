package uddug.com.naukoteka.mvvm.chat

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ChatFolderSelectionItem(
    val dialogId: Long,
    val title: String,
    val subtitle: String?,
    val avatarUrl: String?,
    val initials: String?,
    val isGroup: Boolean,
) : Parcelable
