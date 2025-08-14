package uddug.com.naukoteka.ui.fragments.wall.bottom

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import uddug.com.domain.entities.feed.FeedContainer
import uddug.com.domain.entities.profile.UserProfileFullInfo
import uddug.com.naukoteka.R
import uddug.com.naukoteka.databinding.FragmentWallDetailActionBinding
import uddug.com.naukoteka.global.base.BaseBottomSheetDialogFragment
import uddug.com.naukoteka.presentation.profile.navigation.ContainerView
import uddug.com.naukoteka.ui.activities.main.ContainerActivity.Companion.FEED_ARGS
import uddug.com.naukoteka.utils.text.shareIntent
import uddug.com.naukoteka.utils.viewBinding

class WallDetailActionBottomSheetFragment : BaseBottomSheetDialogFragment(), WallDetailActionView {

    override fun getTheme(): Int = R.style.NauDSBottomSheetDialogTheme
    override val contentView: FragmentWallDetailActionBinding by viewBinding(
        FragmentWallDetailActionBinding::bind
    )


    private var dialogUploadChoose: AlertDialog? = null

    companion object {
        const val PROFILE_FULL_INFO_ARGS = "PROFILE_FULL_INFO_ARGS"
        const val DELETE_AVATAR_RESULT = "DELETE_AVATAR_RESULT"
        const val UPLOAD_AVATAR_RESULT = "UPLOAD_AVATAR_RESULT"
        private const val IMAGE_TYPE_CHANGE = "IMAGE_TYPE_CHANGE"

        fun newInstance(
            profileFullInfo: UserProfileFullInfo,
            feedContainer: FeedContainer,
        ): WallDetailActionBottomSheetFragment {
            return WallDetailActionBottomSheetFragment().apply {
                arguments = bundleOf(
                    PROFILE_FULL_INFO_ARGS to profileFullInfo,
                    FEED_ARGS to feedContainer
                )
            }
        }
    }

    @InjectPresenter
    lateinit var presenter: WallDetailActionPresenter

    @ProvidePresenter
    fun providePresenter(): WallDetailActionPresenter {
        return getScope().getInstance(WallDetailActionPresenter::class.java)
    }

    private var containerNavigation: ContainerView? = null


    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.containerNavigation = requireActivity() as ContainerView
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_wall_detail_action, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.getParcelable<UserProfileFullInfo>(PROFILE_FULL_INFO_ARGS)
            ?.let { presenter.setProfileFullInfo(it) }
        arguments?.getParcelable<FeedContainer>(FEED_ARGS)
            ?.let { presenter.initFeed(it) }

        contentView.shareContainer.setOnClickListener {
            presenter.sharePost()
        }
        contentView.saveContainer.setOnClickListener {
            presenter.addToFavorite()
        }
        contentView.hidePosts.setOnClickListener {
            presenter.hideAuthorPosts()
        }
        contentView.complaintContainer.setOnClickListener {
            presenter.selectPostComplaint()
        }
    }

    override fun showShareDialog(feedContainer: FeedContainer) {

        val shareIntent = Intent()
        shareIntent.action = Intent.ACTION_SEND
        shareIntent.type = "text/plain"
        shareIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        shareIntent.putExtra(
            Intent.EXTRA_TEXT,
            getString(R.string.stage_naukotheka_ru_post, feedContainer.id)
        )
        requireActivity().startActivity(Intent.createChooser(shareIntent, ""))
    }

    override fun showAddedToFavorite(success: Boolean) {
        Toast.makeText(
            requireActivity(),
            getString(R.string.added_to_favorite),
            Toast.LENGTH_LONG
        )
            .show()
    }

    override fun showHideAuthorStatus(success: Boolean) {
        Toast.makeText(
            requireActivity(),
            getString(R.string.author_disabled_from_rec),
            Toast.LENGTH_LONG
        )
            .show()
    }

    override fun openComplaintPost() {

    }

}
