package uddug.com.naukoteka.utils

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import uddug.com.naukoteka.ui.chat.compose.util.uriToFile
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Holds files shared into the app via ACTION_SEND / ACTION_SEND_MULTIPLE.
 * The user is expected to pick a chat in [uddug.com.naukoteka.ui.chat.ChatListFragment];
 * the target [uddug.com.naukoteka.ui.chat.ChatDialogFragment] then consumes the
 * pending files and attaches them to the message input.
 *
 * No Context is stored — callers pass it to [consumeAsFiles]. This keeps the class
 * compatible with both Hilt and the legacy Toothpick DI used by [BaseActivity].
 */
@Singleton
class SharedContentStore @Inject constructor() {

    private val _uris = MutableStateFlow<List<Uri>>(emptyList())
    val uris: StateFlow<List<Uri>> = _uris

    fun hasPending(): Boolean = _uris.value.isNotEmpty()

    fun push(uris: List<Uri>) {
        if (uris.isEmpty()) return
        _uris.value = uris
    }

    fun clear() {
        _uris.value = emptyList()
    }

    /**
     * Copies pending URIs to local files and clears the store.
     * Returns empty list if nothing to consume.
     */
    fun consumeAsFiles(context: Context): List<File> {
        val pending = _uris.value
        if (pending.isEmpty()) return emptyList()
        _uris.value = emptyList()
        return pending.mapNotNull { uri -> uriToFile(context, uri) }
    }
}
