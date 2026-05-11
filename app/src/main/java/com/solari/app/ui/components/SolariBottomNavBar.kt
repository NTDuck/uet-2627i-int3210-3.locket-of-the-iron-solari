package com.solari.app.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
    modifier: Modifier = Modifier
) {
    val items = listOf(
        NavNavItem(SolariRoute.Screen.CameraBefore.name, "Camera", imageVector = Icons.Outlined.PhotoCamera),
        NavNavItem(SolariRoute.Screen.Feed.name, "Feed", drawableRes = R.drawable.grid),
        NavNavItem(
            SolariRoute.Screen.Conversations.name,
            "Chats",
            imageVector = Icons.Outlined.ChatBubbleOutline
        ),
        NavNavItem(SolariRoute.Screen.Profile.name, "Profile", imageVector = Icons.Outlined.PersonOutline)
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
                    if (item.drawableRes != null) {
                        Icon(
                            painter = painterResource(id = item.drawableRes),
                            contentDescription = item.label,
                            tint = tint,
                            modifier = Modifier.size(24.dp)
                        )
                    } else if (item.imageVector != null) {
                        Icon(
                            imageVector = item.imageVector,
                            contentDescription = item.label,
                            tint = tint,
                            modifier = Modifier.size(24.dp)
                        )
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
    val imageVector: ImageVector? = null,
    @param:DrawableRes val drawableRes: Int? = null
)
