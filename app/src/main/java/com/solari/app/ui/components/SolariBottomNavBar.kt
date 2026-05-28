package com.solari.app.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solari.app.R
import com.solari.app.navigation.SolariRoute
import com.solari.app.ui.theme.SolariTheme

@Composable
fun SolariBottomNavBar(
    selectedRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
    showChatsBadge: Boolean = false
) {
    val items = listOf(
        NavNavItem(
            route = SolariRoute.Screen.CameraBefore.name,
            label = "Camera",
            selectedImageVector = Icons.Filled.PhotoCamera,
            unselectedImageVector = Icons.Outlined.PhotoCamera
        ),
        NavNavItem(
            route = SolariRoute.Screen.Feed.name,
            label = "Feed",
            selectedDrawableRes = R.drawable.grid_filled,
            unselectedDrawableRes = R.drawable.grid
        ),
        NavNavItem(
            route = SolariRoute.Screen.Conversations.name,
            label = "Chats",
            selectedImageVector = Icons.Filled.ChatBubble,
            unselectedImageVector = Icons.Outlined.ChatBubbleOutline,
            showBadge = showChatsBadge
        ),
        NavNavItem(
            route = SolariRoute.Screen.Profile.name,
            label = "Profile",
            selectedImageVector = Icons.Filled.Person,
            unselectedImageVector = Icons.Outlined.PersonOutline
        )
    )

    val normalizedSelectedRoute = when {
        selectedRoute.startsWith(SolariRoute.Screen.CameraBefore.name) || selectedRoute.startsWith(
            SolariRoute.Screen.CameraAfter.name
        ) -> SolariRoute.Screen.CameraBefore.name

        selectedRoute.startsWith(SolariRoute.Screen.Feed.name) || selectedRoute.startsWith(
            SolariRoute.Screen.FeedBrowse.name
        ) -> SolariRoute.Screen.Feed.name

        selectedRoute.startsWith(SolariRoute.Screen.Conversations.name) || selectedRoute.startsWith(
            SolariRoute.Screen.Chat.name
        ) -> SolariRoute.Screen.Conversations.name

        selectedRoute.startsWith(SolariRoute.Screen.Profile.name) -> SolariRoute.Screen.Profile.name
        else -> selectedRoute
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(SolariTheme.colors.navBarColor)
            .navigationBarsPadding(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        items.forEach { item ->
            val isSelected = normalizedSelectedRoute == item.route
            val tint = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray
            val drawableRes = if (isSelected) item.selectedDrawableRes else item.unselectedDrawableRes
            val imageVector = if (isSelected) item.selectedImageVector else item.unselectedImageVector
            CompositionLocalProvider(LocalContentColor provides Color.White) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(36.dp))
                        .clickable { onNavigate(item.route) }
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Box {
                        if (drawableRes != null) {
                            Icon(
                                painter = painterResource(id = drawableRes),
                                contentDescription = item.label,
                                tint = tint,
                                modifier = Modifier.size(24.dp)
                            )
                        } else if (imageVector != null) {
                            Icon(
                                imageVector = imageVector,
                                contentDescription = item.label,
                                tint = tint,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        if (item.showBadge) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .align(Alignment.TopEnd)
                                    .offset(x = 2.dp, y = (-1).dp)
                                    .clip(CircleShape)
                                    .background(SolariTheme.colors.primary)
                            )
                        }
                    }
                    Text(
                        text = item.label,
                        color = tint,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

private data class NavNavItem(
    val route: String,
    val label: String,
    val selectedImageVector: ImageVector? = null,
    val unselectedImageVector: ImageVector? = null,
    @param:DrawableRes val selectedDrawableRes: Int? = null,
    @param:DrawableRes val unselectedDrawableRes: Int? = null,
    val showBadge: Boolean = false
)
