package uddug.com.naukoteka.ui.fragments.profile.other

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import uddug.com.domain.entities.feed.FeedContainer
import uddug.com.domain.entities.profile.UserProfileFullInfo
import uddug.com.naukoteka.BuildConfig
import uddug.com.naukoteka.R
import uddug.com.naukoteka.mvvm.profile.OtherProfileUiState
import uddug.com.naukoteka.ui.chat.compose.components.Avatar

@Composable
fun OtherProfileScreen(
    state: OtherProfileUiState,
    onBack: () -> Unit,
    onSubscribe: () -> Unit,
    onMessage: () -> Unit,
    onMore: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when (state) {
            is OtherProfileUiState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary
                )
                BackButton(onBack)
            }

            is OtherProfileUiState.Error -> {
                Text(
                    text = state.message,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.align(Alignment.Center).padding(24.dp)
                )
                BackButton(onBack)
            }

            is OtherProfileUiState.Success -> {
                SuccessContent(
                    state = state,
                    onBack = onBack,
                    onSubscribe = onSubscribe,
                    onMessage = onMessage,
                    onMore = onMore,
                )
            }
        }
    }
}

@Composable
private fun SuccessContent(
    state: OtherProfileUiState.Success,
    onBack: () -> Unit,
    onSubscribe: () -> Unit,
    onMessage: () -> Unit,
    onMore: () -> Unit,
) {
    val profile = state.profile
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            ProfileHeader(profile)
        }
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(Modifier.height(44.dp)) // место под перекрывающий аватар
                Text(
                    text = profile.fullName.orEmpty(),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(6.dp))
                SubtitleLines(profile)
                Spacer(Modifier.height(10.dp))
                Description(profile.dsc)
                Spacer(Modifier.height(14.dp))
                if (!state.isSelf) {
                    ActionRow(
                        isSubscribed = state.isSubscribed,
                        onSubscribe = onSubscribe,
                        onMessage = onMessage,
                        onMore = onMore,
                    )
                    Spacer(Modifier.height(16.dp))
                }
                InfoSection(profile)
                Spacer(Modifier.height(14.dp))
                Divider()
                Spacer(Modifier.height(12.dp))
                CountsRow(
                    subscribers = profile.meta?.subscnCount ?: 0,
                    subscriptions = profile.meta?.subscrCount ?: 0
                )
                Spacer(Modifier.height(12.dp))
            }
        }
        item {
            FeedTabs()
            if (state.feedLoading) {
                Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
        }
        items(state.feed) { feed ->
            FeedPostCard(feed)
        }
    }
    BackButton(onBack)
}

@Composable
private fun ProfileHeader(profile: UserProfileFullInfo) {
    Box(modifier = Modifier.fillMaxWidth()) {
        // Обложка.
        val banner = profile.bannerUrl
        if (!banner.isNullOrEmpty()) {
            AsyncImage(
                model = BuildConfig.IMAGE_SERVER_URL.plus(banner),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
        }
        // Аватар, перекрывающий низ обложки.
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = 16.dp, y = 48.dp)
                .size(96.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
                .padding(3.dp)
        ) {
            Avatar(
                url = profile.image?.path,
                name = profile.fullName,
                size = 90.dp
            )
        }
    }
}

@Composable
private fun SubtitleLines(profile: UserProfileFullInfo) {
    val lines = profile.userAcademicDegree.mapNotNull { it.name?.takeIf { n -> n.isNotBlank() } }
    lines.forEach { line ->
        Text(
            text = line,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp
        )
        Spacer(Modifier.height(2.dp))
    }
}

@Composable
private fun Description(dsc: String?) {
    if (dsc.isNullOrBlank()) return
    var expanded by remember { mutableStateOf(false) }
    Text(
        text = dsc,
        color = MaterialTheme.colorScheme.onSurface,
        fontSize = 14.sp,
        maxLines = if (expanded) Int.MAX_VALUE else 2,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
    )
}

@Composable
private fun ActionRow(
    isSubscribed: Boolean,
    onSubscribe: () -> Unit,
    onMessage: () -> Unit,
    onMore: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (isSubscribed) {
            OutlinedButton(
                onClick = onSubscribe,
                modifier = Modifier.weight(1f).height(44.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_check),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(8.dp))
                Text("Вы подписаны", color = MaterialTheme.colorScheme.primary)
            }
        } else {
            Button(
                onClick = onSubscribe,
                modifier = Modifier.weight(1f).height(44.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_notification_plus),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = Color.White
                )
                Spacer(Modifier.width(8.dp))
                Text("Подписаться", color = Color.White)
            }
        }
        SquareIconButton(iconRes = R.drawable.ic_chat, onClick = onMessage)
        SquareIconButton(iconRes = R.drawable.ic_more, onClick = onMore)
    }
}

@Composable
private fun SquareIconButton(iconRes: Int, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun InfoSection(profile: UserProfileFullInfo) {
    profile.laborActivity.forEach { labor ->
        val position = labor.position?.takeIf { it.isNotBlank() }
        val org = labor.orgName?.takeIf { it.isNotBlank() }
        if (position != null || org != null) {
            InfoRow(iconRes = R.drawable.ic_work_experience) {
                Row {
                    if (position != null) {
                        Text(
                            text = if (org != null) "$position в " else position,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 14.sp
                        )
                    }
                    if (org != null) {
                        Text(
                            text = org,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
    val place = profile.placeOfResidence?.takeIf { it.isNotBlank() }
    if (place != null) {
        InfoRow(iconRes = R.drawable.ic_location) {
            Text(text = place, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp)
        }
    }
    InfoRow(iconRes = R.drawable.ic_more_info) {
        Text(text = "Узнать подробнее", color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
    }
}

@Composable
private fun InfoRow(iconRes: Int, content: @Composable () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            modifier = Modifier.size(18.dp).padding(top = 1.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.width(10.dp))
        content()
    }
}

@Composable
private fun CountsRow(subscribers: Int, subscriptions: Int) {
    Text(
        text = "$subscribers подписчиков · $subscriptions подписок",
        color = MaterialTheme.colorScheme.onSurface,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium
    )
}

@Composable
private fun Divider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
    )
}

@Composable
private fun FeedTabs() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Актуальное",
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.width(20.dp))
        Text(
            text = "Популярное",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 15.sp
        )
        Spacer(Modifier.weight(1f))
        Text(
            text = "Ещё",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 15.sp
        )
    }
}

@Composable
private fun FeedPostCard(feed: FeedContainer) {
    val body = feed.body
    val author = body?.authorInfo
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Avatar(url = author?.imageUrl ?: author?.image, name = author?.fullName, size = 32.dp)
            Spacer(Modifier.width(10.dp))
            Column {
                Text(
                    text = author?.fullName.orEmpty(),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                val date = body?.publicationDate ?: feed.pubDate
                if (!date.isNullOrBlank()) {
                    Text(
                        text = date,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
            }
        }
        val title = body?.title?.takeIf { it.isNotBlank() }
        if (title != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
        val text = body?.text?.takeIf { it.isNotBlank() }
        if (text != null) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = text,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 14.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(Modifier.height(4.dp))
        Divider()
    }
}

@Composable
private fun BackButton(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .padding(start = 12.dp, top = 36.dp)
            .size(36.dp)
            .clip(CircleShape)
            .background(Color(0x66000000))
            .clickable { onBack() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_back),
            contentDescription = "Назад",
            tint = Color.White,
            modifier = Modifier.size(20.dp)
        )
    }
}
