package uddug.com.naukoteka.ui.call

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import uddug.com.naukoteka.ui.theme.NaukotekaTheme

class GroupCallFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                NaukotekaTheme {
                    Surface {
                        Text(text = "Group call is not implemented yet")
                    }
                }
            }
        }
    }

    companion object {
        const val ARG_CONTACT_NAME = "contact_name"
        const val ARG_AVATAR_URL = "avatar_url"
        const val ARG_DIALOG_ID = "dialog_id"
    }
}
