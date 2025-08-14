package uddug.com.naukoteka.ui.fragments.profile.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import uddug.com.domain.entities.feed.FeedContainer
import uddug.com.naukoteka.BuildConfig
import uddug.com.naukoteka.R
import uddug.com.naukoteka.databinding.ItemFeedWallBinding
import uddug.com.naukoteka.databinding.ItemMyFeedWallBinding
import uddug.com.naukoteka.utils.getHashCodeToString
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
        override fun areItemsTheSame(
            oldItem: FeedContainer,
            newItem: FeedContainer
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: FeedContainer,
            newItem: FeedContainer
        ): Boolean {
            return oldItem == newItem
        }
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
    ) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: FeedContainer) {
            binding.root.setOnClickListener {
                onFeedContainerClick(item)
            }
            binding.moreDots.setOnClickListener {
                onPostMenuClick(item)
            }
            binding.postImage.isVisible = false
            binding.name.text = item.body?.authorInfo?.fullName ?: ""
            binding.postTitle.text = item.body?.title
            binding.postDescription.text = item.body?.text
            binding.likeCount.text = item.body?.meta?.firstCommentCount?.toString() ?: "0"
            binding.viewedCount.text = item.body?.meta?.viewCount.toString() ?: "0"
            binding.commentsCount.text = item.body?.meta?.commentCount.toString() ?: "0"
            if (item.body?.authorInfo?.imageUrl.isNotNullOrEmpty()) {
                item.body?.authorInfo?.imageUrl.let {
                    binding.profileImage.load(
                        withAnimation = false,
                        model = BuildConfig.IMAGE_SERVER_URL.plus(item.body?.authorInfo?.imageUrl)
                    )
                }
            } else {
                when (getHashCodeToString(item.body?.authorInfo?.rEntity, 8)) {
                    0 -> binding.profileImage.background =
                        binding.root.context.resources.getDrawable(R.drawable.background_gradient_one)

                    1 -> binding.profileImage.background =
                        binding.root.context.resources.getDrawable(R.drawable.background_gradient_two)

                    2 -> binding.profileImage.background =
                        binding.root.context.resources.getDrawable(R.drawable.background_gradient_three)

                    3 -> binding.profileImage.background =
                        binding.root.context.resources.getDrawable(R.drawable.background_gradient_four)

                    4 -> binding.profileImage.background =
                        binding.root.context.resources.getDrawable(R.drawable.background_gradient_five)

                    5 -> binding.profileImage.background =
                        binding.root.context.resources.getDrawable(R.drawable.background_gradient_six)

                    6 -> binding.profileImage.background =
                        binding.root.context.resources.getDrawable(R.drawable.background_gradient_seven)

                    7 -> binding.profileImage.background =
                        binding.root.context.resources.getDrawable(R.drawable.background_gradient_eight)
                }

                (((item.body?.authorInfo?.fullName?.split(" ")?.firstOrNull()
                    ?.toCharArray()?.firstOrNull()
                    ?: ("" + item.body?.authorInfo?.fullName?.split(" ")?.get(1)?.toCharArray()
                        ?.firstOrNull())) ?: "")).toString()
                binding.initials.isVisible = true
            }

            item.body?.files?.forEach { file ->
                file.contentType?.let {
                    when (detectContentType(it)) {
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

            // Получаем текущее время в Instant
            val now = Instant.now()

            // Сравниваем даты и вычисляем разницу в днях
            val daysAgo = ChronoUnit.DAYS.between(publicationInstant, now)


            if (daysAgo == 0L) {
                binding.postDate.text = binding.root.context.getString(R.string.today_daye)

            } else if (daysAgo == 1L) {
                binding.postDate.text = binding.root.context.getString(R.string.one_day_before)
            } else {
                binding.postDate.text =
                    binding.root.context.getString(R.string.few_days_before, daysAgo.toString())
            }

            if (item.body?.upPost?.authorInfo?.fullName.isNotNullOrEmpty()) {
                binding.originalContainer.isVisible = true
                binding.originalName.text = item.body?.upPost?.authorInfo?.fullName ?: ""
                binding.originalPostTitle.text = item.body?.upPost?.title
                binding.originalPostDescription.text = item.body?.upPost?.text

                if (item.body?.upPost?.authorInfo?.imageUrl.isNotNullOrEmpty()) {
                    item.body?.upPost?.authorInfo?.imageUrl.let {
                        binding.originalProfileImage.load(
                            withAnimation = false,
                            model = BuildConfig.IMAGE_SERVER_URL.plus(item.body?.upPost?.authorInfo?.imageUrl)
                        )
                    }
                } else {
                    when (getHashCodeToString(item.body?.upPost?.authorInfo?.rEntity, 8)) {
                        0 -> binding.originalProfileImage.background =
                            binding.root.context.resources.getDrawable(R.drawable.background_gradient_one)

                        1 -> binding.originalProfileImage.background =
                            binding.root.context.resources.getDrawable(R.drawable.background_gradient_two)

                        2 -> binding.originalProfileImage.background =
                            binding.root.context.resources.getDrawable(R.drawable.background_gradient_three)

                        3 -> binding.originalProfileImage.background =
                            binding.root.context.resources.getDrawable(R.drawable.background_gradient_four)

                        4 -> binding.originalProfileImage.background =
                            binding.root.context.resources.getDrawable(R.drawable.background_gradient_five)

                        5 -> binding.originalProfileImage.background =
                            binding.root.context.resources.getDrawable(R.drawable.background_gradient_six)

                        6 -> binding.originalProfileImage.background =
                            binding.root.context.resources.getDrawable(R.drawable.background_gradient_seven)

                        7 -> binding.originalProfileImage.background =
                            binding.root.context.resources.getDrawable(R.drawable.background_gradient_eight)
                    }

                    (((item.body?.upPost?.authorInfo?.fullName?.split(" ")?.firstOrNull()
                        ?.toCharArray()?.firstOrNull()
                        ?: ("" + item.upPost?.authorInfo?.fullName?.split(" ")?.get(1)
                            ?.toCharArray()
                            ?.firstOrNull())) ?: "")).toString()
                    binding.originalInitials.isVisible = true
                }

//                item.upPost?.files?.forEach { file ->
//                    file.contentType?.let {
//                        when (detectContentType(it)) {
//                            ContentType.IMAGE -> {
//                                binding.postImage.load(
//                                    withAnimation = true,
//                                    model = BuildConfig.IMAGE_SERVER_URL.plus(file.path)
//                                )
//                                binding.postImage.isVisible = true
//                            }
//
//                            ContentType.VIDEO -> ""
//                            ContentType.LINK -> ""
//                            ContentType.NONE -> ""
//                        }
//                    }
//                }
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