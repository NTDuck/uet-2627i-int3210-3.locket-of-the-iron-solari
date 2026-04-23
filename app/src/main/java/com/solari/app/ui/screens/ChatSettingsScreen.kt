package com.solari.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    onNavigateToCamera: () -> Unit,
    onNavigateToFeed: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val isAllLoading = viewModel.isLoading &&
            viewModel.username == null &&
            viewModel.isMuted == null
    val partner = viewModel.partner ?: initialPartner
    val displayName = if (viewModel.isReadOnly) "Someone" else partner?.displayName.orEmpty()
    val displayUsername = if (viewModel.isReadOnly) "someone" else viewModel.username ?: partner?.username.orEmpty()
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
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Chat Settings",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
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
                    .background(MaterialTheme.colorScheme.background)
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
                        color = Color.White
                    )
                    Text(
                        text = "@$displayUsername",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                if (!viewModel.isReadOnly) {
                    Text(
                        text = "PREFERENCES",
                        fontSize = 12.sp * 1.4f,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary,
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
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = MaterialTheme.colorScheme.primary
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
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                SettingsRow(
                    icon = Icons.Default.Delete,
                    title = "Clear Chat History",
                    onClick = { showClearHistoryConfirm = true },
                    trailing = {
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
                    }
                )

                if (!viewModel.isReadOnly) {
                    Spacer(modifier = Modifier.height(8.dp))

                    SettingsRow(
                        icon = Icons.Default.Block,
                        title = "Block User",
                        titleColor = Color(0xFFE57373),
                        onClick = { showBlockConfirm = true },
                        trailing = {
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
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
                showClearHistoryConfirm = false
                viewModel.clearChatHistory(chatId) {
                    onClearHistoryComplete("Chat history cleared")
                }
            },
            onDismiss = { showClearHistoryConfirm = false }
        )
    }

    if (showBlockConfirm) {
        SolariConfirmationDialog(
            title = "Block user?",
            message = "${partner?.displayName ?: "This user"} will no longer be able to interact with you.",
            confirmText = "Block",
            onConfirm = {
                showBlockConfirm = false
                viewModel.blockPartner(onBlocked = onNavigateBack)
            },
            onDismiss = { showBlockConfirm = false }
        )
    }
}
