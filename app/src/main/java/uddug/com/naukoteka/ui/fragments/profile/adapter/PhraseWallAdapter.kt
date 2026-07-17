package uddug.com.naukoteka.ui.fragments.profile.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import uddug.com.domain.entities.feed.FeedContainer
import uddug.com.naukoteka.BuildConfig
import uddug.com.naukoteka.R
import uddug.com.naukoteka.databinding.ItemMyFeedWallBinding
import uddug.com.naukoteka.utils.profileGradientRes
import uddug.com.naukoteka.utils.text.isNotNullOrEmpty
import uddug.com.naukoteka.utils.ui.load
import java.time.Instant
import java.time.temporal.ChronoUnit

class FeedContainerAdapter(
    private val onFeedContainerClick: (FeedContainer) -> Unit,
    private val onPostMenuClick: (FeedContainer) -> Unit,
    private val onLikeClick: (FeedContainer) -> Unit,
    private val onPostImageClick: (FeedContainer) -> Unit
) : ListAdapter<FeedContainer, FeedContainerAdapter.ViewHolder>(
    object : DiffUtil.ItemCallback<FeedContainer>() {
        override fun areItemsTheSame(oldItem: FeedContainer, newItem: FeedContainer): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: FeedContainer, newItem: FeedContainer): Boolean =
            oldItem == newItem
    }
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            onFeedContainerClick = onFeedContainerClick,
            onPostMenuClick = onPostMenuClick,
            onLikeClick = onLikeClick,
            onPostImageClick = onPostImageClick,
            binding = ItemMyFeedWallBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false,
            ),
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }

    inner class ViewHolder(
        val binding: ItemMyFeedWallBinding,
        private val onFeedContainerClick: (FeedContainer) -> Unit,
        private val onPostMenuClick: (FeedContainer) -> Unit,
        private val onLikeClick: (FeedContainer) -> Unit,
        private val onPostImageClick: (FeedContainer) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: FeedContainer) {
            binding.root.setOnClickListener { onFeedContainerClick(item) }
            binding.moreDots.setOnClickListener { onPostMenuClick(item) }
            binding.postImage.isVisible = false
            binding.name.text = item.body?.authorInfo?.fullName ?: ""
            binding.postTitle.text = item.body?.title
            binding.postDescription.text = item.body?.text
            binding.likeCount.text = item.body?.meta?.firstCommentCount?.toString() ?: "0"
            binding.viewedCount.text = item.body?.meta?.viewCount.toString() ?: "0"
            binding.commentsCount.text = item.body?.meta?.commentCount.toString() ?: "0"

            if (item.body?.authorInfo?.imageUrl.isNotNullOrEmpty()) {
                binding.profileImage.load(
                    withAnimation = false,
                    model = BuildConfig.IMAGE_SERVER_URL.plus(item.body?.authorInfo?.imageUrl)
                )
            } else {
                binding.profileImage.background = binding.root.context.resources.getDrawable(
                    profileGradientRes(item.body?.authorInfo?.rEntity)
                )
                binding.initials.isVisible = true
            }

            item.body?.files?.forEach { file ->
                file.contentType?.let { contentTypeStr ->
                    when (detectContentType(contentTypeStr)) {
                        ContentType.IMAGE -> {
                            binding.postImage.load(
                                withAnimation = true,
                                model = BuildConfig.IMAGE_SERVER_URL.plus(file.path)
                            )
                            binding.postImage.isVisible = true
                        }
                        ContentType.VIDEO -> ""
                        ContentType.LINK -> ""
                        ContentType.NONE -> ""
                    }
                }
            }

            val publicationInstant = Instant.parse(item.pubDate)
            val now = Instant.now()
            val daysAgo = ChronoUnit.DAYS.between(publicationInstant, now)

            binding.postDate.text = when (daysAgo) {
                0L -> binding.root.context.getString(R.string.today_daye)
                1L -> binding.root.context.getString(R.string.one_day_before)
                else -> binding.root.context.getString(R.string.few_days_before, daysAgo.toString())
            }

            if (item.body?.upPost?.authorInfo?.fullName.isNotNullOrEmpty()) {
                binding.originalContainer.isVisible = true
                binding.originalName.text = item.body?.upPost?.authorInfo?.fullName ?: ""
                binding.originalPostTitle.text = item.body?.upPost?.title
                binding.originalPostDescription.text = item.body?.upPost?.text

                if (item.body?.upPost?.authorInfo?.imageUrl.isNotNullOrEmpty()) {
                    binding.originalProfileImage.load(
                        withAnimation = false,
                        model = BuildConfig.IMAGE_SERVER_URL.plus(item.body?.upPost?.authorInfo?.imageUrl)
                    )
                } else {
                    binding.originalProfileImage.background =
                        binding.root.context.resources.getDrawable(
                            profileGradientRes(item.body?.upPost?.authorInfo?.rEntity)
                        )
                    binding.originalInitials.isVisible = true
                }
            } else {
                binding.originalContainer.isVisible = false
            }
        }
    }
}

enum class ContentType {
    IMAGE,
    VIDEO,
    LINK,
    NONE
}

fun detectContentType(contentType: String): ContentType {
    return when (contentType) {
        "image/webp" -> ContentType.IMAGE
        else -> ContentType.NONE
    }
}
