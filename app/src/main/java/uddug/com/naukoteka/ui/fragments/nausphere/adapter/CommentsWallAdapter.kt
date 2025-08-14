package uddug.com.naukoteka.ui.fragments.nausphere.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import uddug.com.domain.entities.feed.PostComment
import uddug.com.naukoteka.BuildConfig
import uddug.com.naukoteka.R
import uddug.com.naukoteka.databinding.ItemCommentWallBinding
import uddug.com.naukoteka.utils.getHashCodeToString
import uddug.com.naukoteka.utils.text.isNotNullOrEmpty
import uddug.com.naukoteka.utils.ui.load
import java.time.Instant
import java.time.temporal.ChronoUnit

class CommentsWallAdapter : ListAdapter<PostComment, CommentsWallAdapter.ViewHolder>(
    object : DiffUtil.ItemCallback<PostComment>() {
        override fun areItemsTheSame(oldItem: PostComment, newItem: PostComment): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: PostComment, newItem: PostComment): Boolean =
            oldItem == newItem
    }
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCommentWallBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }

    inner class ViewHolder(
        private val binding: ItemCommentWallBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PostComment) {
            binding.postImage.isVisible = false
            binding.name.text = item.authorInfo?.fullName.orEmpty()
            binding.commentText.text = item.text
            if (item.authorInfo?.imageUrl.isNotNullOrEmpty()) {
                binding.profileImage.load(
                    withAnimation = false,
                    model = BuildConfig.IMAGE_SERVER_URL + item.authorInfo?.imageUrl
                )
                binding.initials.isVisible = false
            } else {
                binding.profileImage.background = getGradientDrawable(
                    binding.root.context,
                    getHashCodeToString(item.authorInfo?.rEntity, 8)
                )
                binding.initials.text = extractInitial(item.authorInfo?.fullName.orEmpty())
                binding.initials.isVisible = true
            }
            binding.postDate.text = item.publicationDate?.let { formatPostDate(it, binding.root.context) }
        }

        private fun formatPostDate(pubDate: String, context: android.content.Context): String {
            return try {
                val publicationInstant = Instant.parse(pubDate)
                when (val daysAgo = ChronoUnit.DAYS.between(publicationInstant, Instant.now())) {
                    0L -> context.getString(R.string.today_daye)
                    1L -> context.getString(R.string.one_day_before)
                    else -> context.getString(R.string.few_days_before, daysAgo.toString())
                }
            } catch (e: Exception) {
                pubDate
            }
        }

        @SuppressLint("UseCompatLoadingForDrawables")
        private fun getGradientDrawable(context: android.content.Context, hash: Int) =
            context.getDrawable(
                when (hash) {
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
            )

        private fun extractInitial(fullName: String): String {
            val words = fullName.trim().split(" ")
            return words.firstOrNull()?.firstOrNull()?.toString() ?: ""
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
