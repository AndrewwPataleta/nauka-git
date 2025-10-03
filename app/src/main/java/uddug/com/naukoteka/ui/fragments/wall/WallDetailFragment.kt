package uddug.com.naukoteka.ui.fragments.wall

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.navigation.fragment.findNavController
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import uddug.com.domain.entities.feed.AuthorInfo
import uddug.com.domain.entities.feed.FeedContainer
import uddug.com.domain.entities.feed.PostComment
import uddug.com.domain.entities.profile.UserProfileFullInfo
import uddug.com.naukoteka.BuildConfig
import uddug.com.naukoteka.R
import uddug.com.naukoteka.databinding.FragmentWallDetailBinding
import uddug.com.naukoteka.global.base.BaseFragment
import uddug.com.naukoteka.presentation.profile.navigation.ContainerNavigationView
import uddug.com.naukoteka.ui.activities.main.ContainerActivity.Companion.FEED_ARGS
import uddug.com.naukoteka.ui.activities.main.ContainerActivity.Companion.PROFILE_ARGS
import uddug.com.naukoteka.ui.fragments.nausphere.adapter.CommentsWallAdapter
import uddug.com.naukoteka.ui.fragments.profile.adapter.ContentType
import uddug.com.naukoteka.ui.fragments.profile.adapter.detectContentType
import uddug.com.naukoteka.utils.getHashCodeToString
import uddug.com.naukoteka.utils.text.isNotNullOrEmpty
import uddug.com.naukoteka.utils.ui.load
import uddug.com.naukoteka.utils.viewBinding
import java.time.Instant
import java.time.temporal.ChronoUnit

class WallDetailFragment : BaseFragment(R.layout.fragment_wall_detail), WallDetailView {

    companion object {
        private const val FRAGMENT_FULL_INFO_TAG = "FRAGMENT_FULL_INFO_TAG"
    }

    override val contentView by viewBinding(FragmentWallDetailBinding::bind)

    @InjectPresenter
    lateinit var presenter: WallDetailPresenter

    private var navigationView: ContainerNavigationView? = null
    private var commentsWallAdapter: CommentsWallAdapter = CommentsWallAdapter()

    @ProvidePresenter
    fun providePresenter(): WallDetailPresenter =
        getScope().getInstance(WallDetailPresenter::class.java)

    override fun onAttach(context: Context) {
        super.onAttach(context)
        navigationView = requireActivity() as? ContainerNavigationView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupArguments()
        setupViews()
    }

    private fun setupArguments() {
        arguments?.getParcelable<UserProfileFullInfo>(PROFILE_ARGS)
            ?.let { presenter.setProfileFullInfo(it) }
        arguments?.getParcelable<FeedContainer>(FEED_ARGS)
            ?.let { presenter.getWallComments(it) }
    }

    private fun setupViews() {
        with(contentView) {
            back.setOnClickListener { findNavController().popBackStack() }
            commentsContainer.adapter = commentsWallAdapter
            sendMessage.setOnClickListener { presenter.sendMessage() }
            currentTextMessage.addTextChangedListener {
                presenter.setCurrentMessageText(it.toString())
            }
        }
    }

    override fun onResume() {
        super.onResume()
        navigationView?.showNavigationBottomBar(false)
    }

    override fun setCurrentFeed(feedContainer: FeedContainer) {
        feedContainer.body?.let { body ->
            with(contentView) {
                postImage.isVisible = false
                name.text = body.authorInfo?.fullName.orEmpty()
                postTitle.text = body.title
                postDescription.text = body.text
                likeCount.text = body.meta?.firstCommentCount?.toString() ?: "0"

                setupAuthorProfileImage(
                    authorInfo = body.authorInfo,
                    imageView = profileImage,
                    initialsView = initials
                )

                body.files?.forEach { file ->
                    file.contentType?.let { contentType ->
                        when (detectContentType(contentType)) {
                            ContentType.IMAGE -> {
                                postImage.load(
                                    withAnimation = true,
                                    model = BuildConfig.IMAGE_SERVER_URL + file.path
                                )
                                postImage.isVisible = true
                            }
                            else -> Unit
                        }
                    }
                }

                postDate.text = feedContainer.pubDate?.let { formatPostDate(it) }

                
                body.upPost?.let { upPost ->
                    if (upPost.authorInfo?.fullName.isNotNullOrEmpty()) {
                        originalContainer.isVisible = true
                        originalName.text = upPost.authorInfo?.fullName.orEmpty()
                        originalPostTitle.text = upPost.title
                        originalPostDescription.text = upPost.text
                        setupAuthorProfileImage(
                            authorInfo = upPost.authorInfo,
                            imageView = originalProfileImage,
                            initialsView = originalInitials
                        )
                    } else {
                        originalContainer.isVisible = false
                    }
                } ?: run {
                    originalContainer.isVisible = false
                }
            }
        }
    }

    private fun formatPostDate(pubDate: String): String {
        return try {
            val publicationInstant = Instant.parse(pubDate)
            val daysAgo = ChronoUnit.DAYS.between(publicationInstant, Instant.now())
            when (daysAgo) {
                0L -> requireContext().getString(R.string.today_daye)
                1L -> requireContext().getString(R.string.one_day_before)
                else -> requireContext().getString(R.string.few_days_before, daysAgo.toString())
            }
        } catch (e: Exception) {
            pubDate
        }
    }

    private fun setupAuthorProfileImage(
        authorInfo: AuthorInfo?,
        imageView: ImageView,
        initialsView: TextView
    ) {
        if (authorInfo?.imageUrl.isNotNullOrEmpty()) {
            imageView.load(
                withAnimation = false,
                model = BuildConfig.IMAGE_SERVER_URL + authorInfo?.imageUrl
            )
            initialsView.isVisible = false
        } else {
            imageView.background = getGradientDrawable(getHashCodeToString(authorInfo?.rEntity, 8))
            initialsView.text = extractInitial(authorInfo?.fullName.orEmpty())
            initialsView.isVisible = true
        }
    }

    private fun getGradientDrawable(hash: Int): Drawable? {
        val drawableRes = when (hash) {
            0 -> R.drawable.background_gradient_one
            1 -> R.drawable.background_gradient_two
            2 -> R.drawable.background_gradient_three
            3 -> R.drawable.background_gradient_four
            4 -> R.drawable.background_gradient_five
            5 -> R.drawable.background_gradient_six
            6 -> R.drawable.background_gradient_seven
            7 -> R.drawable.background_gradient_eight
            else -> R.drawable.background_gradient_one
        }
        return requireContext().getDrawable(drawableRes)
    }

    private fun extractInitial(fullName: String): String {
        val words = fullName.split(" ")
        return when {
            words.firstOrNull()?.isNotEmpty() == true -> words.first().first().toString()
            words.getOrNull(1)?.isNotEmpty() == true -> words[1].first().toString()
            else -> ""
        }
    }

    override fun setWallComments(comments: List<PostComment>) {
        contentView.commentsCount.text =
            getString(R.string.count_of_comments, comments.size.toString())
        commentsWallAdapter.submitList(comments)
    }

    override fun showMessageSendEmpty() {
        Toast.makeText(
            requireActivity(),
            getString(R.string.enter_message_error),
            Toast.LENGTH_LONG
        ).show()
    }

    override fun setCurrentMessageText(messageText: String) {
        contentView.currentTextMessage.setText(messageText)
    }
}
