package com.solari.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.annotation.DrawableRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.solari.app.R
import com.solari.app.navigation.SolariRoute
import com.solari.app.ui.theme.SolariTheme

@Composable
fun SolariBottomNavBar(
    selectedRoute: String,
    onNavigate: (String) -> Unit
) {
    val items = listOf(
        NavNavItem(SolariRoute.Screen.CameraBefore.name, imageVector = Icons.Outlined.PhotoCamera),
        NavNavItem(SolariRoute.Screen.Feed.name, drawableRes = R.drawable.grid),
        NavNavItem(SolariRoute.Screen.Conversations.name, imageVector = Icons.Outlined.ChatBubbleOutline),
        NavNavItem(SolariRoute.Screen.Profile.name, imageVector = Icons.Outlined.PersonOutline)
    )

    val normalizedSelectedRoute = when {
        selectedRoute.startsWith(SolariRoute.Screen.CameraBefore.name) || selectedRoute.startsWith(SolariRoute.Screen.CameraAfter.name) -> SolariRoute.Screen.CameraBefore.name
        selectedRoute.startsWith(SolariRoute.Screen.Feed.name) || selectedRoute.startsWith(SolariRoute.Screen.FeedBrowse.name) -> SolariRoute.Screen.Feed.name
        selectedRoute.startsWith(SolariRoute.Screen.Conversations.name) || selectedRoute.startsWith(SolariRoute.Screen.Chat.name) -> SolariRoute.Screen.Conversations.name
        selectedRoute.startsWith(SolariRoute.Screen.Profile.name) -> SolariRoute.Screen.Profile.name
        else -> selectedRoute
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp, start = 24.dp, end = 24.dp)
            .height(72.dp)
            .clip(RoundedCornerShape(36.dp))
            .background(SolariTheme.colors.navBarColor)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val rippleShape = RoundedCornerShape(36.dp)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(rippleShape)
                        .clickable {
                            onNavigate(item.route)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    val isSelected = normalizedSelectedRoute == item.route
                    val tint = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray
                    if (item.drawableRes != null) {
                        Icon(
                            painter = painterResource(id = item.drawableRes),
                            contentDescription = item.route,
                            tint = tint,
                            modifier = Modifier.size(28.dp)
                        )
                    } else if (item.imageVector != null) {
                        Icon(
                            imageVector = item.imageVector,
                            contentDescription = item.route,
                            tint = tint,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }
    }
}

private data class NavNavItem(
    val route: String,
    val imageVector: ImageVector? = null,
    @param:DrawableRes val drawableRes: Int? = null
)
