package uddug.com.naukoteka.ui.chat.compose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 10.dp, vertical = 10.dp)
    ) {
        items((1..8).toList()) { index ->
            val isMine = index % 2 == 0
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
            ) {
                Box(
                    modifier = Modifier
                        .width(100.dp)
                        .height(30.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(placeholderColor)
                        .shimmer(shimmer)
                )
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
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        items((1..6).toList()) { _ ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .padding(vertical = 8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(placeholderColor)
                    .shimmer(shimmer)
            )
        }
    }
}
