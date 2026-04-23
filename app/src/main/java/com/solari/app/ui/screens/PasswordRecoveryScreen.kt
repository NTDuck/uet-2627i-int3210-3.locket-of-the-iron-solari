package com.solari.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.Text
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
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solari.app.ui.components.SolariBackButton
import com.solari.app.ui.components.SolariButton
import com.solari.app.ui.components.SolariFeedbackPill
import com.solari.app.ui.components.SolariTextField
import com.solari.app.ui.theme.PlusJakartaSans
import com.solari.app.ui.theme.SolariTheme
import com.solari.app.ui.viewmodels.PasswordRecoveryViewModel
import kotlinx.coroutines.delay

@Composable
fun PasswordRecoveryScreen(
    viewModel: PasswordRecoveryViewModel,
    onNavigateBack: () -> Unit,
    onGetRecoveryCode: (String) -> Unit
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val density = LocalDensity.current
    val emailFocusRequester = remember { FocusRequester() }
    val isKeyboardVisible = WindowInsets.ime.getBottom(density) > 0
    var emailBounds by remember { mutableStateOf<Rect?>(null) }
    var feedbackPillVisible by remember { mutableStateOf(false) }
    var feedbackEventId by remember { mutableStateOf(0) }
    val contentOffset by animateDpAsState(
        targetValue = if (isKeyboardVisible) (-96).dp else 0.dp,
        animationSpec = tween(durationMillis = 180),
        label = "PasswordRecoveryContentOffset"
    )

    fun closeKeyboardAndFocus() {
        focusManager.clearFocus(force = true)
        keyboardController?.hide()
    }

    LaunchedEffect(viewModel.errorMessage) {
        if (viewModel.errorMessage != null) {
            feedbackPillVisible = true
            feedbackEventId += 1
        }
    }

    LaunchedEffect(feedbackEventId) {
        if (feedbackEventId > 0) {
            delay(1_300)
            feedbackPillVisible = false
            delay(260)
            viewModel.clearError()
        }
    }

    LaunchedEffect(viewModel.isCodeRequested) {
        if (viewModel.isCodeRequested) {
            val email = viewModel.email.trim()
            viewModel.consumeCodeRequested()
            onGetRecoveryCode(email)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = SolariTheme.colors.background
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SolariTheme.colors.background)
                .pointerInput(Unit) {
                    awaitEachGesture {
                        awaitFirstDown(requireUnconsumed = false)
                        val up = waitForUpOrCancellation(pass = PointerEventPass.Final)
                        val bounds = emailBounds
                        if (up != null && bounds?.contains(up.position) != true) {
                            closeKeyboardAndFocus()
                        }
                    }
                }
        ) {
            SolariBackButton(onClick = onNavigateBack)

            androidx.compose.foundation.layout.Column(
                modifier = Modifier
                    .fillMaxSize()
                    .offset(y = contentOffset)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Recover your password",
                    fontSize = 28.sp,
                    lineHeight = 32.sp,
                    maxLines = 1,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    color = SolariTheme.colors.primary,
                    modifier = Modifier.padding(bottom = 48.dp)
                )

                SolariTextField(
                    value = viewModel.email,
                    onValueChange = { viewModel.email = it },
                    label = "",
                    labelFontSize = 17.sp,
                    textFontSize = 16.sp,
                    placeholder = "Enter your mail address",
                    modifier = Modifier.padding(bottom = 24.dp),
                    textFieldModifier = Modifier
                        .focusRequester(emailFocusRequester)
                        .onGloballyPositioned { emailBounds = it.boundsInRoot() },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            closeKeyboardAndFocus()
                            viewModel.requestCode()
                        }
                    )
                )

                SolariButton(
                    text = "Get Recovery Code",
                    enabled = !viewModel.isSubmitting,
                    onClick = {
                        keyboardController?.hide()
                        focusManager.clearFocus(force = true)
                        viewModel.requestCode()
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            AnimatedVisibility(
                visible = feedbackPillVisible && viewModel.errorMessage != null,
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
                    message = viewModel.errorMessage.orEmpty(),
                    isSuccess = false
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
