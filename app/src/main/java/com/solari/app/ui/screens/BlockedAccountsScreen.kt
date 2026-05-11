package com.solari.app.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solari.app.ui.components.SolariAvatar
import com.solari.app.ui.components.SolariConfirmationDialog
import com.solari.app.ui.models.BlockedUser
import com.solari.app.ui.theme.PlusJakartaSans
import com.solari.app.ui.theme.SolariTheme
import com.solari.app.ui.util.scaledClickable
import com.solari.app.ui.viewmodels.BlockedAccountsViewModel
import java.util.concurrent.TimeUnit

private val BlockedBackground @Composable get() = SolariTheme.colors.background
private val BlockedSurface @Composable get() = SolariTheme.colors.surface
private val BlockedChip @Composable get() = SolariTheme.colors.surfaceVariant
private val BlockedPrimary @Composable get() = SolariTheme.colors.primary
private val BlockedPrimaryContent @Composable get() = SolariTheme.colors.onPrimary
private val BlockedText @Composable get() = SolariTheme.colors.onBackground
private val BlockedSubtle @Composable get() = SolariTheme.colors.onSurfaceVariant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockedAccountsScreen(
    viewModel: BlockedAccountsViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToCamera: () -> Unit,
    onNavigateToFeed: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    var pendingUnblock by remember { mutableStateOf<BlockedUser?>(null) }
    var isUserRefreshing by remember { mutableStateOf(false) }
    val blockedAccounts = viewModel.blockedUsers

    BackHandler(onBack = onNavigateBack)

    LaunchedEffect(viewModel.isLoading) {
        if (!viewModel.isLoading) {
            isUserRefreshing = false
        }
    }

    PullToRefreshBox(
        isRefreshing = isUserRefreshing,
        onRefresh = {
            isUserRefreshing = true
            viewModel.refresh()
        },
        modifier = Modifier
            .fillMaxSize()
            .background(BlockedBackground)
            .navigationBarsPadding()
            .padding(top = 24.dp, bottom = 59.dp)
    ) {
            Column(modifier = Modifier.fillMaxSize()) {
                BlockedAccountsHeader(onNavigateBack = onNavigateBack)

                if (viewModel.isLoading && blockedAccounts.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = SolariTheme.colors.primary,
                            trackColor = SolariTheme.colors.surface
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(
                            start = 24.dp,
                            end = 24.dp,
                            top = 16.dp,
                            bottom = 32.dp
                        )
                    ) {
                        item {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.padding(bottom = 4.dp)
                            ) {
                                listOf("default", "newest", "oldest").forEach { sort ->
                                    BlockedSortChip(
                                        text = sort,
                                        selected = viewModel.sort == sort,
                                        onClick = { viewModel.updateSort(sort) }
                                    )
                                }
                            }
                        }

                        if (blockedAccounts.isEmpty()) {
                            item {
                                Text(
                                    text = viewModel.errorMessage ?: "No blocked accounts",
                                    color = BlockedSubtle,
                                    fontSize = 14.sp,
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(top = 24.dp)
                                )
                            }
                        }

                        items(blockedAccounts) { account ->
                            BlockedAccountItem(
                                account = account,
                                onUnblock = { pendingUnblock = account }
                            )
                        }
                    }
            }
        }
    }

    pendingUnblock?.let { account ->
        SolariConfirmationDialog(
            title = "Unblock ${account.user.displayName}?",
            message = "They will be able to find your profile and interact with visible content again.",
            confirmText = "Unblock",
            onConfirm = {
                viewModel.unblockUser(account.user.id)
                pendingUnblock = null
            },
            onDismiss = { pendingUnblock = null }
        )
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
                .scaledClickable(pressedScale = 1.2f, onClick = onNavigateBack)
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
private fun BlockedAccountItem(
    account: BlockedUser,
    onUnblock: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(BlockedSurface)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SolariAvatar(
            imageUrl = account.user.profileImageUrl,
            username = account.user.username,
            contentDescription = "${account.user.displayName} avatar",
            modifier = Modifier
                .size(width = 48.dp, height = 44.dp),
            shape = RoundedCornerShape(6.dp),
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = account.user.displayName,
                color = BlockedText,
                fontSize = 14.sp,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Blocked ${account.blockedAt.toRelativeTimeLabel()}",
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
                .clickable(onClick = onUnblock)
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

private fun Long.toRelativeTimeLabel(nowMillis: Long = System.currentTimeMillis()): String {
    val elapsedMillis = (nowMillis - this).coerceAtLeast(0L)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(elapsedMillis)
    val hours = TimeUnit.MILLISECONDS.toHours(elapsedMillis)
    val days = TimeUnit.MILLISECONDS.toDays(elapsedMillis)

    return when {
        minutes < 1 -> "just now"
        minutes < 60 -> "${minutes}m ago"
        hours < 24 -> "${hours}h ago"
        days < 7 -> "${days}d ago"
        days < 30 -> "${days / 7}w ago"
        days < 365 -> "${days / 30}mo ago"
        else -> "${days / 365}y ago"
    }
}
