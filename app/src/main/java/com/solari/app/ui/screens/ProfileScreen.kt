package com.solari.app.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.credentials.CredentialManager
import com.solari.app.BuildConfig
import com.solari.app.ui.auth.GoogleIdTokenResult
import com.solari.app.ui.auth.requestGoogleIdToken
import com.solari.app.ui.components.SolariAvatar
import com.solari.app.ui.components.SolariConfirmationDialog
import com.solari.app.ui.theme.PlusJakartaSans
import com.solari.app.ui.theme.SolariTheme
import com.solari.app.ui.util.compressAvatarForUpload
import com.solari.app.ui.util.scaledClickable
import com.solari.app.ui.viewmodels.ProfileViewModel
import com.solari.app.ui.viewmodels.SettingsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    settingsViewModel: SettingsViewModel,
    onNavigateToChangePassword: () -> Unit,
    onNavigateToChangeTheme: () -> Unit,
    onNavigateToManageFriends: () -> Unit,
    onLogout: () -> Unit,
    externalFeedbackMessage: String? = null,
    onExternalFeedbackConsumed: () -> Unit = {}
) {
    val user = viewModel.user
    val context = LocalContext.current
    val credentialManager = remember(context) { CredentialManager.create(context) }
    val focusManager = LocalFocusManager.current
    val outsideEditClickSource = remember { MutableInteractionSource() }
    val coroutineScope = rememberCoroutineScope()
    val feedbackMessage = viewModel.successMessage ?: viewModel.errorMessage
    val isSuccessFeedback = viewModel.successMessage != null
    var pillVisible by remember { mutableStateOf(false) }
    var pillMessage by remember { mutableStateOf("") }
    var pillIsSuccess by remember { mutableStateOf(false) }
    var topPillVisible by remember { mutableStateOf(false) }
    var topPillMessage by remember { mutableStateOf("") }
    var topPillIsSuccess by remember { mutableStateOf(false) }
    var topPillEventId by remember { mutableIntStateOf(0) }
    var suppressNextBottomError by remember { mutableStateOf(false) }
    var routeNextProfileEditFeedbackToTop by remember { mutableStateOf(false) }

    var editingField by remember { mutableStateOf<String?>(null) }
    var tempValue by remember { mutableStateOf("") }

    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showGoogleDeleteConfirm by remember { mutableStateOf(false) }
    var showLogoutConfirm by remember { mutableStateOf(false) }
    var showRemoveAvatarConfirm by remember { mutableStateOf(false) }
    var showRemoveDisplayNameConfirm by remember { mutableStateOf(false) }
    var deletePassword by remember { mutableStateOf("") }
    var selectedAvatarUri by remember { mutableStateOf<Uri?>(null) }
    var committedAvatarPreviewUri by remember { mutableStateOf<Uri?>(null) }
    val isGoogleLinked = viewModel.isSignedInWithGoogle
    val avatarPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedAvatarUri = uri
            viewModel.clearMessages()
        }
    }

    fun openAvatarPicker() {
        avatarPicker.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    fun updateSelectedAvatar() {
        val avatarUri = selectedAvatarUri ?: return
        val previousAvatarPreviewUri = committedAvatarPreviewUri
        selectedAvatarUri = null
        committedAvatarPreviewUri = avatarUri
        viewModel.clearMessages()

        coroutineScope.launch {
            val upload = withContext(Dispatchers.IO) {
                runCatching {
                    compressAvatarForUpload(context, avatarUri)
                }.getOrNull()
            }

            if (upload == null) {
                committedAvatarPreviewUri = previousAvatarPreviewUri
                viewModel.errorMessage = "Failed to read selected avatar"
                return@launch
            }

            viewModel.updateAvatar(
                avatar = upload,
                onSuccess = {
                    committedAvatarPreviewUri = avatarUri
                },
                onFailure = {
                    committedAvatarPreviewUri = previousAvatarPreviewUri
                }
            )
        }
    }

    fun showTopFeedback(message: String, isSuccess: Boolean) {
        topPillMessage = message
        topPillIsSuccess = isSuccess
        topPillVisible = true
        topPillEventId += 1
    }

    fun showTopError(message: String) {
        suppressNextBottomError = true
        showTopFeedback(message = message, isSuccess = false)
    }

    fun deleteAccountWithGoogleVerification() {
        coroutineScope.launch {
            when (
                val tokenResult = requestGoogleIdToken(
                    context = context,
                    credentialManager = credentialManager,
                    serverClientId = BuildConfig.SOLARI_GOOGLE_SERVER_CLIENT_ID
                )
            ) {
                is GoogleIdTokenResult.Success -> {
                    viewModel.deleteAccountWithGoogle(
                        idToken = tokenResult.idToken,
                        onSuccess = {
                            showGoogleDeleteConfirm = false
                            onLogout()
                        },
                        onFailure = ::showTopError
                    )
                }

                is GoogleIdTokenResult.Failure -> showTopError(tokenResult.message)
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(feedbackMessage, isSuccessFeedback) {
        if (feedbackMessage != null) {
            if (!isSuccessFeedback && suppressNextBottomError) {
                suppressNextBottomError = false
                return@LaunchedEffect
            }

            if (routeNextProfileEditFeedbackToTop) {
                routeNextProfileEditFeedbackToTop = false
                showTopFeedback(message = feedbackMessage, isSuccess = isSuccessFeedback)
                return@LaunchedEffect
            }

            pillMessage = feedbackMessage
            pillIsSuccess = isSuccessFeedback
            pillVisible = true

            delay(1000)
            pillVisible = false
            delay(320)
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(externalFeedbackMessage) {
        val message = externalFeedbackMessage ?: return@LaunchedEffect
        pillMessage = message
        pillIsSuccess = true
        pillVisible = true
        onExternalFeedbackConsumed()

        delay(1000)
        pillVisible = false
        delay(320)
    }

    LaunchedEffect(topPillEventId) {
        if (topPillEventId > 0) {
            delay(1000)
            topPillVisible = false
            delay(320)
            viewModel.clearMessages()
        }
    }

    if (user == null) {
        PullToRefreshBox(
            isRefreshing = viewModel.isLoading,
            onRefresh = viewModel::loadMe,
            modifier = Modifier
                .fillMaxSize()
                .background(SolariTheme.colors.background)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (viewModel.isLoading) {
                    CircularProgressIndicator(
                        color = SolariTheme.colors.primary,
                        trackColor = SolariTheme.colors.surface
                    )
                } else {
                    Text(
                        text = viewModel.errorMessage ?: "Profile unavailable",
                        color = SolariTheme.colors.onBackground,
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SolariTheme.colors.background)
            .padding(top = 24.dp)
    ) {
        PullToRefreshBox(
            isRefreshing = viewModel.isLoading,
            onRefresh = viewModel::loadMe,
            modifier = Modifier
                .fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        enabled = editingField != null,
                        interactionSource = outsideEditClickSource,
                        indication = null
                    ) {
                        editingField = null
                        focusManager.clearFocus()
                    }
                    .padding(horizontal = 24.dp),
                contentPadding = PaddingValues(top = 32.dp, bottom = 24.dp)
            ) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box {
                            SolariAvatar(
                                imageUrl = selectedAvatarUri?.toString()
                                    ?: committedAvatarPreviewUri?.toString()
                                    ?: user.profileImageUrl,
                                username = user.username,
                                contentDescription = "Profile Picture",
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(RoundedCornerShape(24.dp))
                                    .clickable { openAvatarPicker() },
                                shape = RoundedCornerShape(24.dp),
                                fontSize = 42.sp
                            )
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .offset(x = 8.dp, y = 8.dp)
                                    .size(32.dp)
                                    .scaledClickable(pressedScale = 1.2f) { openAvatarPicker() }
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        SolariTheme.colors.primary,
                                        RoundedCornerShape(8.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Edit Avatar",
                                    tint = SolariTheme.colors.onPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        AnimatedVisibility(visible = selectedAvatarUri != null) {
                            Row(
                                modifier = Modifier.padding(top = 14.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    onClick = { selectedAvatarUri = null },
                                    enabled = !viewModel.isUpdatingAvatar,
                                    color = SolariTheme.colors.surface,
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Text(
                                        text = "Cancel",
                                        color = SolariTheme.colors.onSurface,
                                        fontSize = 13.sp,
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(
                                            horizontal = 16.dp,
                                            vertical = 8.dp
                                        )
                                    )
                                }

                                Surface(
                                    onClick = ::updateSelectedAvatar,
                                    enabled = !viewModel.isUpdatingAvatar,
                                    color = SolariTheme.colors.primary,
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Text(
                                        text = if (viewModel.isUpdatingAvatar) "Updating..." else "Update avatar",
                                        color = SolariTheme.colors.onPrimary,
                                        fontSize = 13.sp,
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(
                                            horizontal = 16.dp,
                                            vertical = 8.dp
                                        )
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = user.displayName,
                            fontSize = 24.sp,
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            color = SolariTheme.colors.onBackground
                        )
                        Text(
                            text = "@${user.username}",
                            fontSize = 16.sp,
                            fontFamily = PlusJakartaSans,
                            color = SolariTheme.colors.onSurfaceVariant
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(32.dp)) }

                // Username
                item {
                    ProfileInfoBox(label = "USERNAME", value = "@${user.username}")
                }

                item { Spacer(modifier = Modifier.height(8.dp)) }

                // Email
                item {
                    if (editingField == "email" && !isGoogleLinked) {
                        EditableField(
                            label = "EMAIL",
                            value = tempValue,
                            onValueChange = { tempValue = it },
                            onDone = {
                                routeNextProfileEditFeedbackToTop = true
                                viewModel.updateEmail(tempValue)
                                editingField = null
                                focusManager.clearFocus()
                            }
                        )
                    } else {
                        ProfileInfoBox(
                            label = "EMAIL",
                            value = user.email,
                            onClick = if (isGoogleLinked) {
                                null
                            } else {
                                {
                                    tempValue = user.email
                                    editingField = "email"
                                    viewModel.clearMessages()
                                }
                            }
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(8.dp)) }

                // Display Name
                item {
                    if (editingField == "displayName") {
                        EditableField(
                            label = "DISPLAY NAME",
                            value = tempValue,
                            onValueChange = { tempValue = it },
                            onDone = {
                                routeNextProfileEditFeedbackToTop = true
                                viewModel.updateDisplayName(tempValue)
                                editingField = null
                                focusManager.clearFocus()
                            }
                        )
                    } else {
                        ProfileInfoBox(label = "DISPLAY NAME", value = user.displayName, onClick = {
                            tempValue = user.displayName
                            editingField = "displayName"
                            viewModel.clearMessages()
                        })
                    }
                }

                // Remove Display Name Button
                item {
                    if (user.displayName != user.username) {
                        Spacer(modifier = Modifier.height(8.dp))
                        DestructiveProfileActionButton(
                            title = "Remove Display Name",
                            enabled = true,
                            onClick = {
                                editingField = null
                                focusManager.clearFocus()
                                showRemoveDisplayNameConfirm = true
                            }
                        )
                    }
                }

                // Remove Avatar Button
                item {
                    if (user.profileImageUrl != null || committedAvatarPreviewUri != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        DestructiveProfileActionButton(
                            title = "Remove Avatar",
                            enabled = !viewModel.isUpdatingAvatar,
                            onClick = {
                                editingField = null
                                focusManager.clearFocus()
                                showRemoveAvatarConfirm = true
                            }
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(32.dp)) }

                item {
                    Text(
                        text = "SOCIALS",
                        fontSize = 16.sp,
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        color = SolariTheme.colors.secondary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }

                item {
                    SettingsRow(
                        icon = Icons.Default.People,
                        title = "Manage Friends",
                        onClick = onNavigateToManageFriends,
                        trailing = {
                            Icon(
                                Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = SolariTheme.colors.onSurfaceVariant
                            )
                        }
                    )
                }

                item { Spacer(modifier = Modifier.height(32.dp)) }

                item {
                    Text(
                        text = "SETTINGS & SECURITY",
                        fontSize = 16.sp,
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        color = SolariTheme.colors.secondary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }

                if (!isGoogleLinked) {
                    item {
                        SettingsRow(
                            icon = Icons.Default.Lock,
                            title = "Change Password",
                            onClick = onNavigateToChangePassword,
                            trailing = {
                                Icon(
                                    Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = SolariTheme.colors.onSurfaceVariant
                                )
                            }
                        )
                    }

                    item { Spacer(modifier = Modifier.height(8.dp)) }
                }

                item {
                    SettingsRow(
                        icon = Icons.Default.Palette,
                        title = "Change Theme",
                        onClick = onNavigateToChangeTheme,
                        trailing = {
                            Icon(
                                Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = SolariTheme.colors.onSurfaceVariant
                            )
                        }
                    )
                }

                item { Spacer(modifier = Modifier.height(8.dp)) }

                item {
                    SettingsRow(
                        icon = Icons.Default.DarkMode,
                        title = "Toggle Dark Mode",
                        trailing = {
                            Switch(
                                checked = settingsViewModel.isDarkMode,
                                onCheckedChange = { settingsViewModel.toggleDarkMode(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = SolariTheme.colors.onPrimary,
                                    checkedTrackColor = SolariTheme.colors.primary
                                )
                            )
                        }
                    )
                }

                item { Spacer(modifier = Modifier.height(8.dp)) }

                item {
                    SettingsRow(
                        icon = Icons.Default.Widgets,
                        title = "Add the Widget",
                        onClick = {
                            val appWidgetManager =
                                android.appwidget.AppWidgetManager.getInstance(context)
                            val myProvider = android.content.ComponentName(
                                context,
                                com.solari.app.widget.SolariWidgetProvider::class.java
                            )

                            if (appWidgetManager.isRequestPinAppWidgetSupported) {
                                val successCallback = android.app.PendingIntent.getBroadcast(
                                    context,
                                    0,
                                    android.content.Intent(
                                        context,
                                        com.solari.app.widget.WidgetPinReceiver::class.java
                                    ),
                                    android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
                                )
                                appWidgetManager.requestPinAppWidget(
                                    myProvider,
                                    null,
                                    successCallback
                                )
                            }
                        },
                        trailing = {
                            Icon(
                                Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = SolariTheme.colors.onSurfaceVariant
                            )
                        }
                    )
                }

                item { Spacer(modifier = Modifier.height(8.dp)) }

                item {
                    SettingsRow(
                        icon = Icons.AutoMirrored.Filled.Logout,
                        title = "Log Out",
                        onClick = {
                            if (!viewModel.isSigningOut) {
                                showLogoutConfirm = true
                            }
                        },
                        trailing = { }
                    )
                }

                item { Spacer(modifier = Modifier.height(8.dp)) }

                item {
                    DeleteAccountButton(
                        isDeleting = viewModel.isDeletingAccount,
                        onClick = {
                            if (isGoogleLinked) {
                                showGoogleDeleteConfirm = true
                            } else {
                                showDeleteConfirm = true
                            }
                        }
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = pillVisible,
            enter = slideInVertically(
                initialOffsetY = { it * 2 },
                animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing)
            ),
            exit = slideOutVertically(
                targetOffsetY = { it * 2 },
                animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing)
            ),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 18.dp)
        ) {
            ProfileFeedbackPill(
                message = pillMessage,
                isSuccess = pillIsSuccess
            )
        }

        AnimatedVisibility(
            visible = topPillVisible,
            enter = slideInVertically(
                initialOffsetY = { -it * 2 },
                animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing)
            ),
            exit = slideOutVertically(
                targetOffsetY = { -it * 2 },
                animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing)
            ),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            ProfileFeedbackPill(
                message = topPillMessage,
                isSuccess = topPillIsSuccess
            )
        }
    }

    if (showDeleteConfirm) {
        DeleteAccountDialog(
            password = deletePassword,
            onPasswordChange = { deletePassword = it },
            isDeleting = viewModel.isDeletingAccount,
            onConfirm = {
                if (!viewModel.isDeletingAccount) {
                    val password = deletePassword
                    viewModel.deleteAccount(
                        password = password,
                        onSuccess = {
                            showDeleteConfirm = false
                            deletePassword = ""
                            onLogout()
                        },
                        onFailure = { message ->
                            showTopError(message)
                        }
                    )
                }
            },
            onDismiss = {
                if (!viewModel.isDeletingAccount) {
                    showDeleteConfirm = false
                    deletePassword = ""
                }
            }
        )
    }

    if (showGoogleDeleteConfirm) {
        SolariConfirmationDialog(
            title = "Delete account?",
            message = "Google will verify your identity before your account is permanently deleted.",
            confirmText = "Delete Account",
            confirmColor = SolariTheme.colors.error,
            onConfirm = {
                showGoogleDeleteConfirm = false
                deleteAccountWithGoogleVerification()
            },
            onDismiss = { showGoogleDeleteConfirm = false }
        )
    }

    if (showRemoveDisplayNameConfirm) {
        SolariConfirmationDialog(
            title = "Remove display name?",
            message = "Your profile will show your username instead.",
            confirmText = "Remove Display Name",
            onConfirm = {
                viewModel.clearMessages()
                viewModel.removeDisplayName()
                showRemoveDisplayNameConfirm = false
            },
            onDismiss = { showRemoveDisplayNameConfirm = false }
        )
    }

    if (showRemoveAvatarConfirm) {
        SolariConfirmationDialog(
            title = "Remove avatar?",
            message = "Your current avatar will be removed and the default avatar will be shown.",
            confirmText = "Remove Avatar",
            onConfirm = {
                viewModel.clearMessages()
                viewModel.removeAvatar {
                    selectedAvatarUri = null
                    committedAvatarPreviewUri = null
                }
                showRemoveAvatarConfirm = false
            },
            onDismiss = { showRemoveAvatarConfirm = false }
        )
    }

    if (showLogoutConfirm) {
        SolariConfirmationDialog(
            title = "Log out?",
            message = "You will need to sign in again to use Solari.",
            confirmText = if (viewModel.isSigningOut) "Logging Out..." else "Log Out",
            onConfirm = {
                if (viewModel.isSigningOut) return@SolariConfirmationDialog
                viewModel.signOut(onSuccess = onLogout)
                showLogoutConfirm = false
            },
            onDismiss = { showLogoutConfirm = false }
        )
    }
}

@Composable
private fun DestructiveProfileActionButton(
    title: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    DestructiveProfileRow(
        title = title,
        enabled = enabled,
        showWarningIcon = false,
        onClick = onClick
    )
}

@Composable
private fun DeleteAccountButton(
    isDeleting: Boolean,
    onClick: () -> Unit
) {
    DestructiveProfileRow(
        title = "Delete Account",
        enabled = !isDeleting,
        showWarningIcon = true,
        onClick = onClick
    )
}

@Composable
private fun DestructiveProfileRow(
    title: String,
    enabled: Boolean,
    showWarningIcon: Boolean = true,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(18.dp)
    Surface(
        color = SolariTheme.colors.error.copy(alpha = 0.2f),
        shape = shape,
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .clickable(enabled = enabled, onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = SolariTheme.colors.error,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    color = SolariTheme.colors.error,
                    fontSize = 16.sp,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold
                )
            }
            if (showWarningIcon) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = SolariTheme.colors.error,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun DeleteAccountDialog(
    password: String,
    onPasswordChange: (String) -> Unit,
    isDeleting: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = {
            if (!isDeleting) onDismiss()
        }
    ) {
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
                        text = "Delete Account",
                        color = SolariTheme.colors.onSurface,
                        fontSize = 16.sp,
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Enter your password to permanently delete your account.",
                        color = SolariTheme.colors.tertiary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Medium
                    )
                    BasicTextField(
                        value = password,
                        onValueChange = onPasswordChange,
                        singleLine = true,
                        enabled = !isDeleting,
                        visualTransformation = PasswordVisualTransformation(),
                        textStyle = TextStyle(
                            color = SolariTheme.colors.onSurface,
                            fontFamily = PlusJakartaSans,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        cursorBrush = SolidColor(SolariTheme.colors.onSurface),
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
                                if (password.isEmpty()) {
                                    Text(
                                        text = "Password",
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

                DeleteAccountDialogActionRow(
                    text = "Delete Account",
                    textColor = SolariTheme.colors.error,
                    shape = RoundedCornerShape(0.dp),
                    onClick = onConfirm
                )
                DeleteAccountDialogActionRow(
                    text = "Cancel",
                    textColor = SolariTheme.colors.onSurface,
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
private fun DeleteAccountDialogActionRow(
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

@Composable
private fun ProfileFeedbackPill(
    message: String,
    isSuccess: Boolean
) {
    val backgroundColor = if (isSuccess) SolariTheme.colors.onSuccess else SolariTheme.colors.error
    val iconTint = if (isSuccess) SolariTheme.colors.success else SolariTheme.colors.onError

    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 0.dp,
        shadowElevation = 10.dp,
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isSuccess) Icons.Default.Check else Icons.Default.Close,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = message,
                color = SolariTheme.colors.onBackground,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun EditableField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    onDone: () -> Unit
) {
    val editFieldClickSource = remember { MutableInteractionSource() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = editFieldClickSource,
                indication = null,
                onClick = {}
            ),
        verticalAlignment = Alignment.Bottom
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            textStyle = TextStyle(
                fontFamily = PlusJakartaSans,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            ),
            label = {
                Text(
                    label,
                    color = SolariTheme.colors.secondary,
                    fontSize = 13.sp,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold
                )
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onDone() }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = SolariTheme.colors.onSurface,
                unfocusedTextColor = SolariTheme.colors.onSurface,
                focusedBorderColor = SolariTheme.colors.primary,
                unfocusedBorderColor = SolariTheme.colors.onSurfaceVariant
            )
        )

        Spacer(modifier = Modifier.width(12.dp))

        Box(
            modifier = Modifier
                .size(56.dp)
                .scaledClickable(pressedScale = 0.85f, onClick = onDone)
                .clip(RoundedCornerShape(12.dp))
                .background(SolariTheme.colors.primary, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Save $label",
                tint = SolariTheme.colors.onPrimary
            )
        }
    }
}

@Composable
fun ProfileInfoBox(
    label: String,
    value: String,
    onClick: (() -> Unit)? = null
) {
    val shape = RoundedCornerShape(12.dp)
    Surface(
        color = SolariTheme.colors.surface,
        shape = shape,
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    label,
                    color = SolariTheme.colors.secondary,
                    fontSize = 13.sp,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    value,
                    color = SolariTheme.colors.onSurface,
                    fontSize = 16.sp,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Medium
                )
            }
            if (onClick != null) {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = SolariTheme.colors.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun SettingsRow(
    icon: ImageVector,
    title: String,
    titleColor: Color? = null,
    onClick: (() -> Unit)? = null,
    trailing: @Composable () -> Unit
) {
    val actualTitleColor = titleColor ?: SolariTheme.colors.onSurface
    val shape = RoundedCornerShape(12.dp)
    Surface(
        color = SolariTheme.colors.surface,
        shape = shape,
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = if (titleColor == null) SolariTheme.colors.primary else actualTitleColor
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    title,
                    color = actualTitleColor,
                    fontSize = 16.sp,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Medium
                )
            }
            trailing()
        }
    }
}
