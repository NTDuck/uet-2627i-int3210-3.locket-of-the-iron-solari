package com.solari.app.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.DropdownMenu
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solari.app.navigation.SolariRoute
import com.solari.app.ui.components.SolariBottomNavBar
import com.solari.app.ui.theme.PlusJakartaSans
import com.solari.app.ui.viewmodels.FriendManagementViewModel

private val FriendsBackground = Color(0xFF111316)
private val FriendsSurface = Color(0xFF1B1C21)
private val FriendsInput = Color(0xFF080B0E)
private val FriendsPrimary = Color(0xFFFF8426)
private val FriendsPrimaryContent = Color(0xFF5F2900)
private val FriendsText = Color(0xFFE3E2E6)
private val FriendsMuted = Color(0xFFD7C0B2)
private val FriendsSubtle = Color(0xFF9699A1)
private val FriendsButton = Color(0xFF34363B)

private data class FriendListEntry(
    val name: String,
    val handle: String,
    val initials: String,
    val avatarColor: Color
)

@Composable
fun FriendManagementScreen(
    viewModel: FriendManagementViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToBlockedAccounts: () -> Unit,
    onNavigateToCamera: () -> Unit,
    onNavigateToFeed: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    var requestText by remember { mutableStateOf("") }
    val inviteLink = "https://solari-backend.com/usern..."
    val friends = remember {
        listOf(
            FriendListEntry("Alex Rivera", "@arivera_sol", "AR", Color(0xFF79431F)),
            FriendListEntry("Sarah Jenkins", "@sara_h_j", "SJ", Color(0xFF5B341D)),
            FriendListEntry("Marcus Thorne", "@thor_ne", "MT", Color(0xFF6A3000)),
            FriendListEntry("Elena Wu", "@ewu_tech", "EW", Color(0xFF4F1E00))
        )
    }

    Scaffold(
        containerColor = FriendsBackground,
        bottomBar = {
            SolariBottomNavBar(
                selectedRoute = SolariRoute.Screen.Conversations.name,
                onNavigate = { routeName ->
                    when (routeName) {
                        SolariRoute.Screen.CameraBefore.name -> onNavigateToCamera()
                        SolariRoute.Screen.Feed.name -> onNavigateToFeed()
                        SolariRoute.Screen.Conversations.name -> onNavigateToChat()
                        SolariRoute.Screen.Profile.name -> onNavigateToProfile()
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(FriendsBackground)
                .padding(innerPadding)
                .statusBarsPadding()
                .padding(horizontal = 19.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 0.dp, bottom = 22.dp)
        ) {
            item {
                FriendManagementSectionTitle(text = "PERSONAL INVITE")
                Spacer(modifier = Modifier.height(13.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(5.dp))
                        .background(FriendsSurface)
                        .padding(13.dp)
                ) {
                    Text(
                        text = inviteLink,
                        color = FriendsText,
                        fontSize = 12.sp,
                        fontFamily = PlusJakartaSans,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(FriendsInput, RoundedCornerShape(2.dp))
                            .padding(horizontal = 13.dp, vertical = 11.dp)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.6.dp)
                            .background(FriendsPrimary)
                    )

                    Spacer(modifier = Modifier.height(13.dp))

                    FriendsPrimaryButton(
                        text = "Share invite link",
                        icon = Icons.Outlined.Share,
                        onClick = {
                            clipboardManager.setText(AnnotatedString(inviteLink))
                            Toast.makeText(context, "Invite link copied", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }

            item {
                FriendManagementSectionTitle(text = "ADD NEW CONNECTION")
                Spacer(modifier = Modifier.height(13.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(5.dp))
                        .background(FriendsSurface)
                        .padding(13.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .height(32.dp)
                            .background(FriendsInput, RoundedCornerShape(topStart = 2.dp))
                    ) {
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.PersonAdd,
                                contentDescription = null,
                                tint = FriendsMuted,
                                modifier = Modifier.size(14.dp)
                            )

                            Spacer(modifier = Modifier.width(10.dp))

                            BasicTextField(
                                value = requestText,
                                onValueChange = { requestText = it },
                                modifier = Modifier.weight(1f),
                                textStyle = TextStyle(
                                    color = FriendsText,
                                    fontFamily = PlusJakartaSans,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                cursorBrush = SolidColor(FriendsText),
                                singleLine = true,
                                decorationBox = { innerTextField ->
                                    Box(contentAlignment = Alignment.CenterStart) {
                                        if (requestText.isEmpty()) {
                                            Text(
                                                text = "username/email",
                                                color = FriendsSubtle,
                                                fontSize = 11.sp,
                                                fontFamily = PlusJakartaSans,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                        innerTextField()
                                    }
                                }
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.6.dp)
                                .background(FriendsMuted)
                        )
                    }

                    Spacer(modifier = Modifier.width(11.dp))

                    Box(
                        modifier = Modifier
                            .weight(0.75f)
                            .height(32.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(FriendsButton)
                            .clickable {
                                requestText = ""
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Send request",
                            color = FriendsText,
                            fontSize = 11.sp,
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

            }

            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(FriendsSurface)
                        .clickable(onClick = onNavigateToBlockedAccounts)
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = "View blocked accounts",
                        color = FriendsMuted,
                        fontSize = 13.sp,
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FriendManagementSectionTitle(text = "FRIEND LIST")
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Filter friends",
                        tint = FriendsMuted,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            items(friends) { friend ->
                FriendListItem(
                    name = friend.name,
                    handle = friend.handle,
                    initials = friend.initials,
                    avatarColor = friend.avatarColor
                )
            }
        }
    }
}

@Composable
private fun FriendManagementSectionTitle(text: String) {
    Text(
        text = text,
        color = FriendsMuted,
        fontSize = 13.sp,
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun FriendsPrimaryButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(38.dp)
            .clip(RoundedCornerShape(19.dp))
            .background(FriendsPrimary)
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = FriendsPrimaryContent,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            color = FriendsPrimaryContent,
            fontSize = 13.sp,
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun FriendListItem(
    name: String,
    handle: String,
    initials: String,
    avatarColor: Color
) {
    var isMenuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clip(RoundedCornerShape(5.dp))
            .background(FriendsSurface)
            .padding(horizontal = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(avatarColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                color = FriendsMuted,
                fontSize = 13.sp,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(13.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(0.dp, Alignment.CenterVertically)
        ) {
            Text(
                text = name,
                color = FriendsText,
                fontSize = 13.sp,
                lineHeight = 13.sp,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = handle,
                color = FriendsSubtle,
                fontSize = 11.sp,
                lineHeight = 11.sp,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Medium
            )
        }

        Box {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Friend actions",
                tint = FriendsMuted,
                modifier = Modifier
                    .size(30.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { isMenuExpanded = true }
                    .padding(6.dp)
            )

            DropdownMenu(
                expanded = isMenuExpanded,
                onDismissRequest = { isMenuExpanded = false },
                containerColor = FriendsSurface
            ) {
                FriendActionMenuItem(
                    text = "Unfriend",
                    color = FriendsText,
                    onClick = { isMenuExpanded = false }
                )
                FriendActionMenuItem(
                    text = "Block",
                    color = FriendsMuted,
                    onClick = { isMenuExpanded = false }
                )
            }
        }
    }
}

@Composable
private fun FriendActionMenuItem(
    text: String,
    color: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 15.sp,
            lineHeight = 15.sp,
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.Medium
        )
    }
}
