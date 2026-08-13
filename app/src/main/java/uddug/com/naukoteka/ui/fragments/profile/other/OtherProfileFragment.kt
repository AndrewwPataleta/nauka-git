package uddug.com.naukoteka.ui.fragments.profile.other

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import uddug.com.naukoteka.R
import uddug.com.naukoteka.mvvm.profile.OtherProfileEvent
import uddug.com.naukoteka.mvvm.profile.OtherProfileViewModel
import uddug.com.naukoteka.ui.chat.ChatDialogFragment.Companion.INTERLOCUTOR_ID
import uddug.com.naukoteka.ui.theme.NaukotekaTheme

@AndroidEntryPoint
class OtherProfileFragment : Fragment() {

    private val viewModel: OtherProfileViewModel by viewModels()

    private val userId: String by lazy { arguments?.getString(ARG_USER_ID).orEmpty() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Свой профиль открываем в отдельной вкладке, а не в «чужом».
        if (userId.isNotEmpty() && viewModel.isSelf(userId)) {
            findNavController().popBackStack()
            findNavController().navigate(R.id.profileFragment)
            return
        }

        if (userId.isNotEmpty()) {
            viewModel.load(userId)
        }
        setupObservers()
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.events.collectLatest { event ->
                when (event) {
                    is OtherProfileEvent.OpenDialog -> {
                        findNavController().navigate(
                            R.id.chatDialogFragment,
                            Bundle().apply { putString(INTERLOCUTOR_ID, event.interlocutorId) }
                        )
                    }
                    OtherProfileEvent.OpenMore -> {
                        // Меню действий профиля — пока заглушка.
                    }
                    is OtherProfileEvent.ShowError -> {
                        Toast.makeText(requireContext(), event.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                NaukotekaTheme {
                    val state by viewModel.uiState.collectAsState()
                    OtherProfileScreen(
                        state = state,
                        onBack = { findNavController().popBackStack() },
                        onSubscribe = { viewModel.onSubscribeClick() },
                        onMessage = { viewModel.onMessageClick() },
                        onMore = { viewModel.onMoreClick() },
                    )
                }
            }
        }
    }

    companion object {
        const val ARG_USER_ID = "userId"
    }
}
