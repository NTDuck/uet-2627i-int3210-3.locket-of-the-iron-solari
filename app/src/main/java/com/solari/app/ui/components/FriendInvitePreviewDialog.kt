package com.solari.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.solari.app.ui.theme.PlusJakartaSans
import com.solari.app.ui.theme.SolariTheme
import com.solari.app.ui.viewmodels.FriendInvitePreviewState
import com.solari.app.ui.viewmodels.FriendInviteRelationship

@Composable
fun FriendInvitePreviewDialog(
    state: FriendInvitePreviewState,
    onPrimaryAction: () -> Unit,
    onCancel: () -> Unit
) {
    Dialog(onDismissRequest = onCancel) {
        Surface(
            color = SolariTheme.colors.surface,
            shape = RoundedCornerShape(16.dp),
            shadowElevation = 12.dp,
            modifier = Modifier.width(284.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                FriendInvitePreviewContent(state = state)

                if (state.user != null && state.relationship != FriendInviteRelationship.Self) {
                    FriendInviteActionRow(
                        text = state.primaryButtonText(),
                        textColor = state.primaryButtonColor(),
                        enabled = true,
                        shape = RoundedCornerShape(0.dp),
                        onClick = onPrimaryAction
                    )
                }
                FriendInviteActionRow(
                    text = "Cancel",
                    textColor = SolariTheme.colors.onSurface,
                    enabled = true,
                    shape = RoundedCornerShape(0.dp),
                    modifier = Modifier.padding(bottom = 28.dp),
                    onClick = onCancel
                )
            }
        }
    }
}

@Composable
private fun FriendInvitePreviewContent(state: FriendInvitePreviewState) {
    Column(
        modifier = Modifier.padding(start = 18.dp, end = 18.dp, top = 36.dp, bottom = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        when {
            state.isLoading -> {
                CircularProgressIndicator(
                    color = SolariTheme.colors.primary,
                    trackColor = SolariTheme.colors.background,
                    modifier = Modifier.size(32.dp)
                )
                Text(
                    text = "Loading profile",
                    color = SolariTheme.colors.onSurface,
                    fontSize = 16.sp,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }

            state.user != null -> {
                SolariAvatar(
                    imageUrl = state.user.profileImageUrl,
                    username = state.user.username,
                    contentDescription = "${state.user.displayName} avatar",
                    modifier = Modifier.size(180.dp),
                    shape = RoundedCornerShape(15.dp),
                    fontSize = 20.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = state.user.displayName,
                    color = SolariTheme.colors.onSurface,
                    fontSize = 20.sp,
                    lineHeight = 20.sp,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "@${state.user.username}",
                    color = SolariTheme.colors.tertiary,
                    fontSize = 14.sp,
                    lineHeight = 15.sp,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                state.relationshipLabel()?.let { label ->
                    Text(
                        text = label,
                        color = SolariTheme.colors.tertiary,
                        fontSize = 15.sp,
                        lineHeight = 18.sp,
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            else -> {
                Text(
                    text = "Profile unavailable",
                    color = SolariTheme.colors.onSurface,
                    fontSize = 16.sp,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }

        state.errorMessage?.let { message ->
            Text(
                text = message,
                color = Color(0xFFE57373),
                fontSize = 15.sp,
                lineHeight = 18.sp,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun FriendInviteActionRow(
    text: String,
    textColor: Color,
    enabled: Boolean,
    shape: RoundedCornerShape,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(42.dp)
            .clip(shape)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (enabled) textColor else SolariTheme.colors.tertiary,
            fontSize = 16.sp,
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

private fun FriendInvitePreviewState.relationshipLabel(): String? {
    return when (relationship) {
        FriendInviteRelationship.Self -> "This is you!"
        FriendInviteRelationship.None -> null
        FriendInviteRelationship.PendingOutgoing -> "You already sent a friend request to this user"
        FriendInviteRelationship.Friend -> "You are already friends with this user"
        FriendInviteRelationship.Blocked -> "You have blocked this user"
    }
}

private fun FriendInvitePreviewState.primaryButtonText(): String {
    return when (relationship) {
        FriendInviteRelationship.Self -> "Cancel"
        FriendInviteRelationship.None -> "Send Friend Request"
        FriendInviteRelationship.PendingOutgoing -> "Unsend request"
        FriendInviteRelationship.Friend -> "Unfriend"
        FriendInviteRelationship.Blocked -> "Unblock"
    }
}

private fun FriendInvitePreviewState.primaryButtonColor(): Color {
    return when (relationship) {
        FriendInviteRelationship.Self,
        FriendInviteRelationship.None,
        FriendInviteRelationship.PendingOutgoing,
        FriendInviteRelationship.Blocked -> Color(0xFFFF8426)
        FriendInviteRelationship.Friend -> Color(0xFFE57373)
    }
}
