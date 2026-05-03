package com.solari.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solari.app.ui.components.SolariButton
import com.solari.app.ui.components.SolariConfirmationDialog
import com.solari.app.ui.components.SolariTextField
import com.solari.app.ui.theme.PlusJakartaSans
import com.solari.app.ui.util.scaledClickable
import com.solari.app.ui.viewmodels.PasswordResetViewModel
import kotlinx.coroutines.delay

@Composable
fun PasswordResetScreen(
    viewModel: PasswordResetViewModel,
    showTopBar: Boolean = true,
    onNavigateBack: () -> Unit,
    onResetComplete: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val density = LocalDensity.current
    val isKeyboardVisible = WindowInsets.ime.getBottom(density) > 0
    val oldPasswordFocusRequester = remember { FocusRequester() }
    val newPasswordFocusRequester = remember { FocusRequester() }
    val confirmPasswordFocusRequester = remember { FocusRequester() }
    var showConfirmation by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var errorPillVisible by remember { mutableStateOf(false) }
    var errorEventId by remember { mutableStateOf(0) }

    fun closeKeyboardAndFocus() {
        focusManager.clearFocus(force = true)
        keyboardController?.hide()
    }

    fun handleBack() {
        if (isKeyboardVisible) {
            closeKeyboardAndFocus()
        } else {
            onNavigateBack()
        }
    }

    BackHandler(onBack = ::handleBack)

    LaunchedEffect(viewModel.errorMessage) {
        if (viewModel.errorMessage != null) {
            errorPillVisible = true
            errorEventId += 1
        }
    }

    LaunchedEffect(errorEventId) {
        if (errorEventId > 0) {
            delay(1000)
            errorPillVisible = false
            delay(260)
            viewModel.clearError()
        }
    }

    LaunchedEffect(viewModel.successMessage) {
        if (viewModel.successMessage != null) {
            viewModel.clearSuccess()
            showSuccessDialog = true
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                if (showTopBar) {
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
                                .scaledClickable(pressedScale = 1.2f, onClick = ::handleBack),
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
                            text = "Change password",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                verticalArrangement = if (!showTopBar) Arrangement.Center else Arrangement.Top
            ) {
                SolariTextField(
                    value = viewModel.oldPassword,
                    onValueChange = { viewModel.oldPassword = it },
                    label = "Old Password",
                    placeholder = "••••••••",
                    isPassword = true,
                    modifier = Modifier.padding(bottom = 24.dp),
                    textFieldModifier = Modifier.focusRequester(oldPasswordFocusRequester),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(
                        onNext = { newPasswordFocusRequester.requestFocus() }
                    )
                )

                SolariTextField(
                    value = viewModel.newPassword,
                    onValueChange = { viewModel.newPassword = it },
                    label = "New Password",
                    placeholder = "••••••••",
                    isPassword = true,
                    modifier = Modifier.padding(bottom = 24.dp),
                    textFieldModifier = Modifier.focusRequester(newPasswordFocusRequester),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(
                        onNext = { confirmPasswordFocusRequester.requestFocus() }
                    )
                )

                SolariTextField(
                    value = viewModel.confirmPassword,
                    onValueChange = { viewModel.confirmPassword = it },
                    label = "Confirm New Password",
                    placeholder = "••••••••",
                    isPassword = true,
                    modifier = Modifier.padding(bottom = 40.dp),
                    textFieldModifier = Modifier.focusRequester(confirmPasswordFocusRequester),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = { closeKeyboardAndFocus() }
                    )
                )

                SolariButton(
                    text = if (viewModel.isSubmitting) "Submitting..." else "Submit",
                    onClick = {
                        closeKeyboardAndFocus()
                        showConfirmation = true
                    }
                )
            }
        }

        AnimatedVisibility(
            visible = errorPillVisible && viewModel.errorMessage != null,
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
            PasswordResetErrorPill(message = viewModel.errorMessage.orEmpty())
        }

    }

    if (showConfirmation) {
        SolariConfirmationDialog(
            title = "Change password?",
            message = "Your password will be updated immediately.",
            confirmText = "Submit",
            confirmColor = MaterialTheme.colorScheme.primary,
            onConfirm = {
                showConfirmation = false
                viewModel.submit()
            },
            onDismiss = { showConfirmation = false }
        )
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {},
            title = {
                Text(
                    text = "Password updated",
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    text = "Password updated successfully, please sign in again.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSuccessDialog = false
                        onResetComplete()
                    }
                ) {
                    Text(
                        text = "OK",
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

@Composable
private fun PasswordResetErrorPill(message: String) {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        shape = MaterialTheme.shapes.large,
        tonalElevation = 0.dp,
        shadowElevation = 10.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = message,
                color = MaterialTheme.colorScheme.onErrorContainer,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
