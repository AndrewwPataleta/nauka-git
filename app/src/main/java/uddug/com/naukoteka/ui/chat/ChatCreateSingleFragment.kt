package uddug.com.naukoteka.ui.chat


import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import uddug.com.domain.entities.chat.Chat
import uddug.com.naukoteka.R
import uddug.com.naukoteka.mvvm.chat.ChatCreateSingleEvent
import uddug.com.naukoteka.mvvm.chat.ChatCreateSingleViewModel
import uddug.com.naukoteka.presentation.profile.navigation.ContainerNavigationView
import uddug.com.naukoteka.ui.chat.ChatDialogFragment.Companion.INTERLOCUTOR_ID
import uddug.com.naukoteka.ui.chat.compose.ChatCreateSingleScreen
import uddug.com.naukoteka.ui.theme.NaukotekaTheme

@AndroidEntryPoint
class ChatCreateSingleFragment : Fragment() {

    private var navigationView: ContainerNavigationView? = null

    private val viewModel: ChatCreateSingleViewModel by viewModels()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        navigationView = requireActivity() as ContainerNavigationView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()
    }


    override fun onResume() {
        super.onResume()
        navigationView?.showNavigationBottomBar(true)
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.events.collectLatest { state ->
                when (state) {
                    is ChatCreateSingleEvent.OpenDialogDetail -> {
                        val navController = findNavController()
                        navController.getBackStackEntry(R.id.chatListFragment).savedStateHandle["refreshChats"] = true
                        navController.navigate(
                            R.id.chatDialogFragment,
                            Bundle().apply { putString(INTERLOCUTOR_ID, state.interlocutorId) },
                            navOptions {
                                popUpTo(R.id.chatCreateSingleFragment) { inclusive = true }
                            }
                        )
                    }
                    ChatCreateSingleEvent.OpenMultiCreate -> {
                        findNavController().navigate(R.id.chatCreateMultiFragment)
                    }
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        return ComposeView(requireContext()).apply {
            setContent {
                NaukotekaTheme {
                    ChatCreateSingleScreen(
                        viewModel = viewModel,
                        onGroupCreateClick = {
                            viewModel.onGroupCreateClick()
                        },
                        onBackPressed = {
                            findNavController().popBackStack()
                        }
                    )
                }
            }
        }
    }

    private fun showLoading() {

    }

    private fun showChats(chats: List<Chat>) {

    }

    private fun showError(message: String) {

    }
}