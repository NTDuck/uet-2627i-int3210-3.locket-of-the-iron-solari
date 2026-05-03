package com.solari.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solari.app.ui.components.SolariBackButton
import com.solari.app.ui.components.SolariButton
import com.solari.app.ui.components.SolariFeedbackPill
import com.solari.app.ui.components.SolariTextField
import com.solari.app.ui.theme.SolariTheme
import com.solari.app.ui.viewmodels.CompletePasswordResetViewModel
import kotlinx.coroutines.delay

@Composable
fun CompletePasswordResetScreen(
    viewModel: CompletePasswordResetViewModel,
    email: String,
    onNavigateBack: () -> Unit,
    onResetComplete: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val density = LocalDensity.current
    val isKeyboardVisible = WindowInsets.ime.getBottom(density) > 0
    val newPasswordFocusRequester = remember { FocusRequester() }
    val confirmPasswordFocusRequester = remember { FocusRequester() }
    var feedbackPillVisible by remember { mutableStateOf(false) }
    var feedbackPillMessage by remember { mutableStateOf("") }
    var feedbackPillIsSuccess by remember { mutableStateOf(false) }
    var feedbackEventId by remember { mutableStateOf(0) }
    val contentOffset by animateDpAsState(
        targetValue = if (isKeyboardVisible) (-96).dp else 0.dp,
        animationSpec = tween(durationMillis = 180),
        label = "CompletePasswordResetContentOffset"
    )

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

    LaunchedEffect(email) {
        viewModel.applyEmail(email)
    }

    LaunchedEffect(viewModel.errorMessage) {
        val message = viewModel.errorMessage ?: return@LaunchedEffect
        feedbackPillMessage = message
        feedbackPillIsSuccess = false
        feedbackPillVisible = true
        feedbackEventId += 1
    }

    LaunchedEffect(feedbackEventId) {
        if (feedbackEventId > 0 && !feedbackPillIsSuccess) {
            delay(1_300)
            feedbackPillVisible = false
            delay(260)
            viewModel.clearError()
        }
    }

    LaunchedEffect(viewModel.successMessage) {
        val message = viewModel.successMessage ?: return@LaunchedEffect
        feedbackPillMessage = message
        feedbackPillIsSuccess = true
        feedbackPillVisible = true
        delay(600)
        viewModel.clearSuccess()
        onResetComplete()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = SolariTheme.colors.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            SolariBackButton(onClick = ::handleBack)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .offset(y = contentOffset)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SolariTextField(
                    value = viewModel.newPassword,
                    onValueChange = { viewModel.newPassword = it },
                    label = "New Password",
                    placeholder = "••••••••",
                    isPassword = true,
                    labelFontSize = 17.sp,
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
                    labelFontSize = 17.sp,
                    modifier = Modifier.padding(bottom = 40.dp),
                    textFieldModifier = Modifier.focusRequester(confirmPasswordFocusRequester),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            closeKeyboardAndFocus()
                            viewModel.submit()
                        }
                    )
                )

                SolariButton(
                    text = "Submit",
                    enabled = !viewModel.isSubmitting,
                    onClick = {
                        closeKeyboardAndFocus()
                        viewModel.submit()
                    },
                    fontSize = 16.sp,
                    modifier = Modifier.fillMaxWidth(0.7f)
                )
            }

            AnimatedVisibility(
                visible = feedbackPillVisible,
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
                SolariFeedbackPill(
                    message = feedbackPillMessage,
                    isSuccess = feedbackPillIsSuccess
                )
            }

            if (viewModel.isSubmitting) {
                CircularProgressIndicator(
                    color = SolariTheme.colors.primary,
                    trackColor = SolariTheme.colors.surface,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}
