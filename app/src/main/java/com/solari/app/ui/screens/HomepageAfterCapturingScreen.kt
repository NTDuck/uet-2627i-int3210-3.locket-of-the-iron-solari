package com.solari.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.solari.app.navigation.SolariRoute
import com.solari.app.ui.components.SolariBottomNavBar
import com.solari.app.ui.theme.PlusJakartaSans
import com.solari.app.ui.theme.SolariTheme
import com.solari.app.ui.viewmodels.HomepageAfterCapturingViewModel

private val CapturePreviewImage =
    "https://www.politicon.com/wp-content/uploads/2017/06/Charlie-Kirk-2019-1024x1024.jpg"

@Composable
fun HomepageAfterCapturingScreen(
    viewModel: HomepageAfterCapturingViewModel,
    onNavigateBack: () -> Unit,
    onSend: () -> Unit,
    onCancel: () -> Unit,
    onNavigateToFeed: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    var selectedFriends by remember { mutableStateOf(setOf<String>()) }
    var isPublic by remember { mutableStateOf(true) }
    val friends = viewModel.friends

    Scaffold(
        containerColor = SolariTheme.colors.background,
        bottomBar = {
            SolariBottomNavBar(
                selectedRoute = SolariRoute.Screen.CameraAfter.name,
                onNavigate = { routeName ->
                    when (routeName) {
                        SolariRoute.Screen.CameraBefore.name -> Unit
                        SolariRoute.Screen.Feed.name -> onNavigateToFeed()
                        SolariRoute.Screen.Conversations.name -> onNavigateToChat()
                        SolariRoute.Screen.Profile.name -> onNavigateToProfile()
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SolariTheme.colors.background)
                .padding(innerPadding)
                .padding(top = 24.dp, start = 24.dp, end = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(36.dp))

            CapturePreviewCard()

            Spacer(modifier = Modifier.height(30.dp))

            Text(
                text = "Visibility",
                color = SolariTheme.colors.onBackground.copy(alpha = 0.86f),
                fontSize = 16.sp,
                lineHeight = 18.sp,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(end = 6.dp)
            ) {
                item {
                    VisibilityAllItem(
                        selected = isPublic,
                        total = friends.size + 1,
                        onClick = {
                            isPublic = true
                            selectedFriends = emptySet()
                        }
                    )
                }

                items(friends) { friend ->
                    val isSelected = friend.id in selectedFriends

                    VisibilityFriendItem(
                        name = friend.displayName,
                        avatarUrl = friend.profileImageUrl,
                        selected = isSelected,
                        onClick = {
                            isPublic = false
                            selectedFriends = if (isSelected) {
                                selectedFriends - friend.id
                            } else {
                                selectedFriends + friend.id
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            CaptureActionButtons(
                onCancel = onCancel,
                onSend = onSend
            )

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun CapturePreviewCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(SolariTheme.colors.surface)
    ) {
        AsyncImage(
            model = CapturePreviewImage,
            contentDescription = "Captured image",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.12f))
        )

        Text(
            text = "Lorem ipsum dolor sit amet,\nconsectetur adipiscing elit.",
            color = Color.White,
            fontSize = 14.sp,
            lineHeight = 19.sp,
            fontFamily = PlusJakartaSans,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 15.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Black.copy(alpha = 0.58f))
                .padding(horizontal = 24.dp, vertical = 9.dp)
        )
    }
}

@Composable
private fun VisibilityAllItem(
    selected: Boolean,
    total: Int,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier.width(56.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .border(
                    width = 2.dp,
                    color = if (selected) SolariTheme.colors.primary else Color.Transparent,
                    shape = CircleShape
                )
                .background(SolariTheme.colors.surfaceVariant, CircleShape)
                .clip(CircleShape)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Public,
                contentDescription = null,
                tint = if (selected) SolariTheme.colors.primary else SolariTheme.colors.onSurfaceVariant.copy(alpha = 0.55f),
                modifier = Modifier.size(20.dp)
            )
        }

        Text(
            text = "All ($total)",
            color = if (selected) SolariTheme.colors.primary else SolariTheme.colors.onBackground.copy(alpha = 0.72f),
            fontSize = 11.sp,
            lineHeight = 12.sp,
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 9.dp)
        )
    }
}

@Composable
private fun VisibilityFriendItem(
    name: String,
    avatarUrl: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier.width(56.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = avatarUrl,
            contentDescription = "$name avatar",
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .border(
                    width = if (selected) 2.dp else 0.dp,
                    color = if (selected) SolariTheme.colors.primary else Color.Transparent,
                    shape = CircleShape
                )
                .clickable(onClick = onClick),
            contentScale = ContentScale.Crop
        )

        Text(
            text = name,
            color = if (selected) SolariTheme.colors.primary else SolariTheme.colors.onBackground.copy(alpha = 0.82f),
            fontSize = 11.sp,
            lineHeight = 12.sp,
            fontFamily = PlusJakartaSans,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 9.dp)
        )
    }
}

@Composable
private fun CaptureActionButtons(
    onCancel: () -> Unit,
    onSend: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .padding(bottom = 36.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(end = 210.dp)
                .size(64.dp)
                .clip(CircleShape)
                .background(Color(0xFF34363B))
                .clickable(onClick = onCancel),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Cancel",
                tint = SolariTheme.colors.onSurface,
                modifier = Modifier.size(27.dp)
            )
        }

        Box(
            modifier = Modifier
                .size(86.dp)
                .clip(CircleShape)
                .background(SolariTheme.colors.primary)
                .border(5.dp, Color(0xFF3A2517), CircleShape)
                .clickable(onClick = onSend),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "Send",
                tint = SolariTheme.colors.onPrimary,
                modifier = Modifier.size(39.dp)
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(start = 210.dp)
                .size(64.dp)
                .clip(CircleShape)
                .background(Color(0xFF34363B))
                .clickable { },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit",
                tint = SolariTheme.colors.onSurface,
                modifier = Modifier.size(27.dp)
            )
        }
    }
}
