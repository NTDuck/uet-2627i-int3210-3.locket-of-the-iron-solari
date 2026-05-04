package com.solari.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solari.app.ui.components.SolariAvatar
import com.solari.app.ui.components.SolariConfirmationDialog
import com.solari.app.ui.components.SolariFeedbackPill
import com.solari.app.ui.models.User
import com.solari.app.ui.theme.SolariTheme
import com.solari.app.ui.util.scaledClickable
import com.solari.app.ui.viewmodels.ChatSettingsViewModel

@Composable
fun ChatSettingsScreen(
    chatId: String,
    initialPartner: User?,
    viewModel: ChatSettingsViewModel,
    onNavigateBack: () -> Unit,
    onClearHistoryComplete: (String) -> Unit,
) {
    val isAllLoading = viewModel.isLoading &&
            viewModel.username == null &&
            viewModel.isMuted == null
    val partner = viewModel.partner ?: initialPartner
    val displayName = if (viewModel.isReadOnly) "Someone" else partner?.displayName.orEmpty()
    val displayUsername =
        if (viewModel.isReadOnly) "someone" else viewModel.username ?: partner?.username.orEmpty()
    val displayAvatarUrl = if (viewModel.isReadOnly) null else partner?.profileImageUrl
    var showClearHistoryConfirm by remember { mutableStateOf(false) }
    var showBlockConfirm by remember { mutableStateOf(false) }
    var topPillVisible by remember { mutableStateOf(false) }
    var topPillMessage by remember { mutableStateOf("") }

    LaunchedEffect(viewModel.successMessage) {
        val message = viewModel.successMessage ?: return@LaunchedEffect
        topPillMessage = message
        topPillVisible = true
    }

    LaunchedEffect(chatId) {
        viewModel.setInitialPartner(initialPartner)
        viewModel.loadSettings(chatId)
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .scaledClickable(pressedScale = 1.2f, onClick = onNavigateBack),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = SolariTheme.colors.primary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Chat Settings",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = SolariTheme.colors.onBackground
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (isAllLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(SolariTheme.colors.background),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = SolariTheme.colors.primary,
                        trackColor = SolariTheme.colors.surface
                    )
                }
                return@Scaffold
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(SolariTheme.colors.background)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    SolariAvatar(
                        imageUrl = displayAvatarUrl,
                        username = displayUsername,
                        contentDescription = "Friend Avatar",
                        modifier = Modifier
                            .size(100.dp),
                        shape = CircleShape,
                        fontSize = 34.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = displayName,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = SolariTheme.colors.onBackground
                    )
                    Text(
                        text = "@$displayUsername",
                        fontSize = 14.sp,
                        color = SolariTheme.colors.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                if (!viewModel.isReadOnly) {
                    Text(
                        text = "PREFERENCES",
                        fontSize = 12.sp * 1.4f,
                        fontWeight = FontWeight.Bold,
                        color = SolariTheme.colors.secondary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    SettingsRow(
                        icon = Icons.Default.Notifications,
                        title = "Mute Notifications",
                        trailing = {
                            Switch(
                                checked = viewModel.isMuted ?: false,
                                onCheckedChange = { viewModel.toggleMute(chatId) },
                                enabled = !viewModel.isLoading && viewModel.isMuted != null,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = SolariTheme.colors.onPrimary,
                                    checkedTrackColor = SolariTheme.colors.primary
                                )
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(32.dp))
                }

                Text(
                    text = "ACTIONS",
                    fontSize = 12.sp * 1.4f,
                    fontWeight = FontWeight.Bold,
                    color = SolariTheme.colors.secondary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                SettingsRow(
                    icon = Icons.Default.Delete,
                    title = "Clear Chat History",
                    onClick = { showClearHistoryConfirm = true },
                    trailing = {
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = SolariTheme.colors.onSurfaceVariant
                        )
                    }
                )

                if (!viewModel.isReadOnly) {
                    Spacer(modifier = Modifier.height(8.dp))

                    SettingsRow(
                        icon = Icons.Default.Block,
                        title = "Block User",
                        titleColor = SolariTheme.colors.error,
                        onClick = { showBlockConfirm = true },
                        trailing = {
                            Icon(
                                Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = SolariTheme.colors.onSurfaceVariant
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }

            AnimatedVisibility(
                visible = topPillVisible,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 12.dp, start = 24.dp, end = 24.dp)
            ) {
                SolariFeedbackPill(
                    message = topPillMessage,
                    isSuccess = true
                )
            }
        }
    }

    if (showClearHistoryConfirm) {
        SolariConfirmationDialog(
            title = "Clear chat history?",
            message = "This removes the visible message history on your side.",
            confirmText = "Clear",
            onConfirm = {
                viewModel.clearChatHistory(chatId) {
                    onClearHistoryComplete("Chat history cleared")
                }
            },
            onDismiss = { }
        )
    }

    if (showBlockConfirm) {
        SolariConfirmationDialog(
            title = "Block user?",
            message = "${partner?.displayName ?: "This user"} will no longer be able to interact with you.",
            confirmText = "Block",
            onConfirm = {
                viewModel.blockPartner(onBlocked = onNavigateBack)
            },
            onDismiss = { }
        )
    }
}
