package com.solari.app.ui.screens

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.solari.app.navigation.SolariRoute
import com.solari.app.ui.components.SolariBottomNavBar
import com.solari.app.ui.theme.PlusJakartaSans
import com.solari.app.ui.theme.SolariTheme
import com.solari.app.ui.viewmodels.BlockedAccountsViewModel

private val BlockedBackground = Color(0xFF111316)
private val BlockedSurface = Color(0xFF1B1C21)
private val BlockedChip = Color(0xFF34363B)
private val BlockedPrimary = Color(0xFFFF8426)
private val BlockedPrimaryContent = Color(0xFF5F2900)
private val BlockedText = Color(0xFFE3E2E6)
private val BlockedMuted = Color(0xFFD7C0B2)
private val BlockedSubtle = Color(0xFF9699A1)

private data class BlockedAccountEntry(
    val name: String,
    val blockedAt: String,
    val avatarUrl: String = "https://www.politicon.com/wp-content/uploads/2017/06/Charlie-Kirk-2019-1024x1024.jpg"
)

@Composable
fun BlockedAccountsScreen(
    viewModel: BlockedAccountsViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToCamera: () -> Unit,
    onNavigateToFeed: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    var selectedSort by remember { mutableStateOf("default") }
    val blockedAccounts = remember {
        listOf(
            BlockedAccountEntry("Kaelen Thorne", "Blocked 2 days ago"),
            BlockedAccountEntry("Marcus Sterling", "Blocked 1 week ago"),
            BlockedAccountEntry("Elena Vane", "Blocked 3 weeks ago"),
            BlockedAccountEntry("Jasper Wu", "Blocked 1 month ago"),
            BlockedAccountEntry("Aria Lund", "Blocked 2 months ago")
        )
    }

    Scaffold(
        containerColor = BlockedBackground,
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BlockedBackground)
                .padding(innerPadding)
        ) {
            BlockedAccountsHeader(onNavigateBack = onNavigateBack)

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 32.dp)
            ) {
                item {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        listOf("default", "newest", "oldest").forEach { sort ->
                            BlockedSortChip(
                                text = sort,
                                selected = selectedSort == sort,
                                onClick = { selectedSort = sort }
                            )
                        }
                    }
                }

                items(blockedAccounts) { account ->
                    BlockedAccountItem(account = account)
                }
            }
        }
    }
}

@Composable
private fun BlockedAccountsHeader(onNavigateBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .padding(top = 12.dp, start = 24.dp, end = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = BlockedPrimary,
            modifier = Modifier
                .size(22.dp)
                .clickable(onClick = onNavigateBack)
        )

        Spacer(modifier = Modifier.width(20.dp))

        Text(
            text = "Blocked Accounts",
            color = BlockedText,
            fontSize = 20.sp,
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun BlockedSortChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .height(36.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(if (selected) BlockedPrimary else BlockedChip)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (selected) BlockedPrimaryContent else BlockedText,
            fontSize = 14.sp,
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun BlockedAccountItem(account: BlockedAccountEntry) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(BlockedSurface)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = account.avatarUrl,
            contentDescription = "${account.name} avatar",
            modifier = Modifier
                .size(width = 48.dp, height = 44.dp)
                .clip(RoundedCornerShape(6.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = account.name,
                color = BlockedText,
                fontSize = 14.sp,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = account.blockedAt,
                color = BlockedSubtle,
                fontSize = 11.sp,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Medium
            )
        }

        Box(
            modifier = Modifier
                .height(36.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(BlockedChip)
                .clickable { }
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Unblock",
                color = SolariTheme.colors.secondary,
                fontSize = 14.sp,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
