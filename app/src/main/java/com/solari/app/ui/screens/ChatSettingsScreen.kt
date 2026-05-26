package com.solari.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.solari.app.ui.components.SolariAvatar
import com.solari.app.ui.components.SolariConfirmationDialog
import com.solari.app.ui.components.SolariFeedbackPill
import com.solari.app.ui.models.User
import com.solari.app.ui.theme.PlusJakartaSans
import com.solari.app.ui.theme.SolariTheme
import com.solari.app.ui.util.scaledClickable
import com.solari.app.ui.viewmodels.ChatSettingsViewModel
import kotlinx.coroutines.delay

private enum class ChatSettingsNicknameAction {
    Set,
    Update
}

private data class ChatSettingsNicknameDialogState(
    val partner: User,
    val action: ChatSettingsNicknameAction
)

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
    val hasNickname = !partner?.nickname.isNullOrBlank()
    var showClearHistoryConfirm by remember { mutableStateOf(false) }
    var showBlockConfirm by remember { mutableStateOf(false) }
    var showRemoveNicknameConfirm by remember { mutableStateOf(false) }
    var nicknameDialogState by remember { mutableStateOf<ChatSettingsNicknameDialogState?>(null) }
    var topPillVisible by remember { mutableStateOf(false) }
    var topPillMessage by remember { mutableStateOf("") }
    var topPillIsSuccess by remember { mutableStateOf(true) }
    var topPillEventId by remember { mutableStateOf(0) }
    val feedbackMessage = viewModel.successMessage ?: viewModel.errorMessage
    val isSuccessFeedback = viewModel.successMessage != null

    LaunchedEffect(feedbackMessage, isSuccessFeedback) {
        val message = feedbackMessage ?: return@LaunchedEffect
        topPillMessage = message
        topPillIsSuccess = isSuccessFeedback
        topPillVisible = true
        topPillEventId += 1
        viewModel.clearMessages()
    }

    LaunchedEffect(topPillEventId) {
        if (topPillEventId > 0) {
            delay(1000)
            topPillVisible = false
        }
    }

    LaunchedEffect(chatId) {
        viewModel.setInitialPartner(initialPartner)
        viewModel.loadSettings(chatId)
    }

    Box(modifier = Modifier.fillMaxSize()) {
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
                    .background(SolariTheme.colors.background)
            ) {
                if (isAllLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
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
                        .padding(innerPadding)
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

                    if (!viewModel.isReadOnly && partner != null) {
                        SettingsRow(
                            icon = Icons.Default.Edit,
                            title = if (hasNickname) "Update nickname" else "Set nickname",
                            onClick = {
                                nicknameDialogState = ChatSettingsNicknameDialogState(
                                    partner = partner,
                                    action = if (hasNickname) {
                                        ChatSettingsNicknameAction.Update
                                    } else {
                                        ChatSettingsNicknameAction.Set
                                    }
                                )
                            },
                            trailing = {
                                Icon(
                                    Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = SolariTheme.colors.onSurfaceVariant
                                )
                            }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        if (hasNickname) {
                            SettingsRow(
                                icon = Icons.Default.Delete,
                                title = "Remove nickname",
                                onClick = { showRemoveNicknameConfirm = true },
                                trailing = {
                                    Icon(
                                        Icons.Default.ChevronRight,
                                        contentDescription = null,
                                        tint = SolariTheme.colors.onSurfaceVariant
                                    )
                                }
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

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
            }
        }

        AnimatedVisibility(
            visible = topPillVisible,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 24.dp, start = 24.dp, end = 24.dp)
        ) {
            SolariFeedbackPill(
                message = topPillMessage,
                isSuccess = topPillIsSuccess
            )
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
                showClearHistoryConfirm = false
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
                viewModel.blockPartner(onBlocked = onNavigateBack)
                showBlockConfirm = false
            },
            onDismiss = { showBlockConfirm = false }
        )
    }

    if (showRemoveNicknameConfirm) {
        SolariConfirmationDialog(
            title = "Remove nickname?",
            message = "${partner?.displayName ?: "This user"} will be shown by their profile name again.",
            confirmText = "Remove nickname",
            onConfirm = {
                viewModel.removeNickname(chatId)
                showRemoveNicknameConfirm = false
            },
            onDismiss = { showRemoveNicknameConfirm = false }
        )
    }

    nicknameDialogState?.let { state ->
        ChatSettingsNicknameDialog(
            state = state,
            onConfirm = { nickname ->
                if (nickname.isNotBlank()) {
                    when (state.action) {
                        ChatSettingsNicknameAction.Set -> viewModel.setNickname(chatId, nickname)
                        ChatSettingsNicknameAction.Update -> viewModel.updateNickname(chatId, nickname)
                    }
                    nicknameDialogState = null
                }
            },
            onDismiss = { nicknameDialogState = null }
        )
    }
}

@Composable
private fun ChatSettingsNicknameDialog(
    state: ChatSettingsNicknameDialogState,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var nickname by remember(state.partner.id, state.action) {
        mutableStateOf(
            if (state.action == ChatSettingsNicknameAction.Update) {
                state.partner.nickname.orEmpty()
            } else {
                ""
            }
        )
    }
    val title = when (state.action) {
        ChatSettingsNicknameAction.Set -> "Set nickname"
        ChatSettingsNicknameAction.Update -> "Update nickname"
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = SolariTheme.colors.surface,
            shape = RoundedCornerShape(16.dp),
            shadowElevation = 12.dp,
            modifier = Modifier.width(284.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Column(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = title,
                        color = SolariTheme.colors.onBackground,
                        fontSize = 16.sp,
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold
                    )
                    BasicTextField(
                        value = nickname,
                        onValueChange = { nickname = it.take(40) },
                        singleLine = true,
                        textStyle = TextStyle(
                            color = SolariTheme.colors.onBackground,
                            fontFamily = PlusJakartaSans,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        cursorBrush = SolidColor(SolariTheme.colors.onBackground),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(SolariTheme.colors.surfaceVariant)
                            .padding(horizontal = 12.dp),
                        decorationBox = { innerTextField ->
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (nickname.isEmpty()) {
                                    Text(
                                        text = "Nickname",
                                        color = SolariTheme.colors.onSurfaceVariant,
                                        fontSize = 14.sp,
                                        fontFamily = PlusJakartaSans
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                }

                ChatSettingsDialogActionRow(
                    text = title,
                    textColor = SolariTheme.colors.primary,
                    shape = RoundedCornerShape(0.dp),
                    onClick = { onConfirm(nickname) }
                )
                ChatSettingsDialogActionRow(
                    text = "Cancel",
                    textColor = SolariTheme.colors.onBackground,
                    shape = RoundedCornerShape(
                        topStart = 0.dp,
                        topEnd = 0.dp,
                        bottomEnd = 16.dp,
                        bottomStart = 16.dp
                    ),
                    onClick = onDismiss
                )
            }
        }
    }
}

@Composable
private fun ChatSettingsDialogActionRow(
    text: String,
    textColor: Color,
    shape: RoundedCornerShape,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(42.dp)
            .clip(shape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 14.sp,
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.Bold
        )
    }
}
