package uddug.com.naukoteka.ui.chat.compose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import com.valentinilk.shimmer.Shimmer
import com.valentinilk.shimmer.ShimmerBounds
import com.valentinilk.shimmer.rememberShimmer
import com.valentinilk.shimmer.shimmer
import uddug.com.naukoteka.R

@Composable
fun ChatListShimmer() {
    val shimmer: Shimmer = rememberShimmer(
        shimmerBounds = ShimmerBounds.View
    )
    val placeholderColor = colorResource(id = R.color.main_background_input_stroke)
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items((1..10).toList()) { _ ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .shimmer(shimmer),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(placeholderColor)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .height(14.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(placeholderColor)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.4f)
                            .height(12.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(placeholderColor)
                    )
                }
            }
        }
    }
}

@Composable
fun MessageListShimmer() {
    val shimmer: Shimmer = rememberShimmer(
        shimmerBounds = ShimmerBounds.View
    )
    val placeholderColor = colorResource(id = R.color.main_background_input_stroke)
    // Pseudo-random but deterministic skeleton shapes that look like a chat.
    // Repeated so the list fills the whole visible area on any screen size.
    val basePattern = listOf(
        MessageSkeleton(isMine = false, bubbleWidthFraction = 0.55f, lineCount = 2, hasAvatar = true),
        MessageSkeleton(isMine = true, bubbleWidthFraction = 0.45f, lineCount = 1, hasAvatar = false),
        MessageSkeleton(isMine = false, bubbleWidthFraction = 0.70f, lineCount = 3, hasAvatar = true),
        MessageSkeleton(isMine = true, bubbleWidthFraction = 0.60f, lineCount = 2, hasAvatar = false),
        MessageSkeleton(isMine = false, bubbleWidthFraction = 0.35f, lineCount = 1, hasAvatar = true),
        MessageSkeleton(isMine = true, bubbleWidthFraction = 0.50f, lineCount = 2, hasAvatar = false),
        MessageSkeleton(isMine = false, bubbleWidthFraction = 0.65f, lineCount = 2, hasAvatar = true),
        MessageSkeleton(isMine = true, bubbleWidthFraction = 0.40f, lineCount = 1, hasAvatar = false),
    )
    val skeletons = List(3) { basePattern }.flatten()
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .shimmer(shimmer),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        itemsIndexed(skeletons) { index, skeleton ->
            MessageSkeletonRow(
                skeleton = skeleton,
                placeholderColor = placeholderColor,
            )
        }
    }
}

private data class MessageSkeleton(
    val isMine: Boolean,
    val bubbleWidthFraction: Float,
    val lineCount: Int,
    val hasAvatar: Boolean,
)

@Composable
private fun MessageSkeletonRow(
    skeleton: MessageSkeleton,
    placeholderColor: androidx.compose.ui.graphics.Color,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (skeleton.isMine) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom,
    ) {
        if (!skeleton.isMine) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(
                        if (skeleton.hasAvatar) placeholderColor
                        else androidx.compose.ui.graphics.Color.Transparent,
                    )
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Column(
            horizontalAlignment = if (skeleton.isMine) Alignment.End else Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxWidth(skeleton.bubbleWidthFraction),
        ) {
            val bubbleShape = if (skeleton.isMine) {
                RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 4.dp)
            } else {
                RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 16.dp)
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(bubbleShape)
                    .background(placeholderColor)
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    repeat(skeleton.lineCount) { line ->
                        val lineFraction = when {
                            line == skeleton.lineCount - 1 && skeleton.lineCount > 1 -> 0.6f
                            else -> 1f
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(lineFraction)
                                .height(10.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    androidx.compose.ui.graphics.Color.White.copy(alpha = 0.55f)
                                )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatDetailShimmer() {
    val shimmer: Shimmer = rememberShimmer(
        shimmerBounds = ShimmerBounds.View
    )
    val placeholderColor = colorResource(id = R.color.main_background_input_stroke)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .shimmer(shimmer),
    ) {
        // Header: big avatar + name + subtitle (users list)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(placeholderColor)
            )
            Spacer(modifier = Modifier.height(14.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.4f)
                    .height(20.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(placeholderColor)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(14.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(placeholderColor)
            )
        }

        // Action buttons row: Call / Share / More
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            repeat(3) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(64.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(placeholderColor)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tab row placeholder (3 tabs)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            repeat(3) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.25f)
                        .height(14.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(placeholderColor)
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        // tab divider
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(placeholderColor)
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Search bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(placeholderColor)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Participant rows (avatar + two lines of text)
        repeat(6) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(placeholderColor)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .height(14.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(placeholderColor)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.3f)
                            .height(12.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(placeholderColor)
                    )
                }
            }
        }
    }
}
