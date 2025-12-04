package uddug.com.naukoteka.ui.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import uddug.com.domain.entities.profile.UserProfileFullInfo
import uddug.com.naukoteka.mvvm.chat.SendContactViewModel
import uddug.com.naukoteka.ui.chat.compose.SendContactComponent
import uddug.com.naukoteka.ui.theme.NaukotekaTheme

@AndroidEntryPoint
class SendContactFragment : Fragment() {

    private val viewModel: SendContactViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                NaukotekaTheme {
                    SendContactComponent(
                        viewModel = viewModel,
                        onBack = { requireActivity().onBackPressed() },
                        onSelect = { user ->
                            onContactSelected(user)
                        }
                    )
                }
            }
        }
    }

    private fun onContactSelected(user: UserProfileFullInfo) {
        findNavController().previousBackStackEntry?.savedStateHandle?.set("selectedUser", user)
        findNavController().popBackStack()
    }
}

