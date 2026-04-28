package com.solari.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solari.app.ui.components.SolariBackButton
import com.solari.app.ui.components.SolariButton
import com.solari.app.ui.components.SolariFeedbackPill
import com.solari.app.ui.theme.PlusJakartaSans
import com.solari.app.ui.theme.SolariTheme
import com.solari.app.ui.util.scaledClickable
import com.solari.app.ui.viewmodels.OTPConfirmationViewModel
import kotlinx.coroutines.delay

@Composable
fun OTPConfirmationScreen(
    viewModel: OTPConfirmationViewModel,
    email: String,
    onNavigateBack: () -> Unit,
    onConfirmComplete: (String) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val codeFocusRequester = remember { FocusRequester() }
    var otpTextFieldValue by remember {
        mutableStateOf(TextFieldValue(viewModel.otpCode, selection = TextRange(viewModel.otpCode.length)))
    }
    var isCodeFocused by remember { mutableStateOf(false) }
    var feedbackPillVisible by remember { mutableStateOf(false) }
    var feedbackEventId by remember { mutableStateOf(0) }

    LaunchedEffect(email) {
        viewModel.applyEmail(email)
    }

    LaunchedEffect(viewModel.otpCode) {
        if (otpTextFieldValue.text != viewModel.otpCode) {
            otpTextFieldValue = TextFieldValue(
                text = viewModel.otpCode,
                selection = TextRange(viewModel.otpCode.length)
            )
        }
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

    LaunchedEffect(viewModel.isVerified) {
        if (viewModel.isVerified) {
            viewModel.consumeVerified()
            onConfirmComplete(viewModel.email)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = SolariTheme.colors.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            SolariBackButton(onClick = onNavigateBack)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "OTP has been sent to",
                    color = SolariTheme.colors.onSurfaceVariant,
                    fontSize = 18.sp,
                    fontFamily = PlusJakartaSans
                )
                Text(
                    text = viewModel.email,
                    color = SolariTheme.colors.primary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = PlusJakartaSans,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "Enter the code to continue",
                    color = SolariTheme.colors.onSurfaceVariant,
                    fontSize = 18.sp,
                    fontFamily = PlusJakartaSans,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                BasicTextField(
                    value = otpTextFieldValue,
                    onValueChange = { newValue ->
                        val filteredCode = newValue.text.filter(Char::isDigit).take(6)
                        otpTextFieldValue = TextFieldValue(
                            text = filteredCode,
                            selection = TextRange(filteredCode.length)
                        )
                        viewModel.onOtpChanged(filteredCode)
                    },
                    singleLine = true,
                    textStyle = TextStyle(color = Color.Transparent),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                        .height(48.dp)
                        .focusRequester(codeFocusRequester)
                        .onFocusChanged { isCodeFocused = it.isFocused },
                    decorationBox = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            repeat(6) { index ->
                                OtpSlot(
                                    value = viewModel.otpCode.getOrNull(index)?.toString().orEmpty(),
                                    isFocused = isCodeFocused && index == focusedOtpSlotIndex(viewModel.otpCode.length),
                                    onClick = {
                                        codeFocusRequester.requestFocus()
                                        keyboardController?.show()
                                    }
                                )
                            }
                        }
                    }
                )

                SolariButton(
                    text = "Confirm",
                    enabled = !viewModel.isSubmitting,
                    onClick = {
                        keyboardController?.hide()
                        viewModel.verify()
                    },
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                val resendShape = RoundedCornerShape(14.dp)
                Surface(
                    color = Color.Transparent,
                    shape = resendShape,
                    modifier = Modifier
                        .scaledClickable(
                            pressedScale = 1.08f,
                            enabled = !viewModel.isResending,
                            onClick = viewModel::resend
                        )
                        .wrapContentWidth()
                        .height(48.dp)
                        .padding(bottom = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(resendShape)
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (viewModel.isResending) {
                                CircularProgressIndicator(
                                    color = SolariTheme.colors.secondary,
                                    trackColor = SolariTheme.colors.surface,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text(
                                text = "Resend OTP",
                                color = SolariTheme.colors.secondary,
                                fontWeight = FontWeight.Bold,
                                fontFamily = PlusJakartaSans,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Didn't receive a code? Check your spam folder.",
                    color = SolariTheme.colors.onSurfaceVariant,
                    fontSize = 14.sp,
                    fontFamily = PlusJakartaSans,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
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

@Composable
private fun OtpSlot(
    value: String,
    isFocused: Boolean,
    onClick: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (isFocused) SolariTheme.colors.primary else SolariTheme.colors.onSurfaceVariant.copy(alpha = 0.35f),
        animationSpec = tween(durationMillis = 60),
        label = "OtpSlotBorderColor"
    )
    val elevation by animateDpAsState(
        targetValue = if (isFocused) 12.dp else 0.dp,
        animationSpec = tween(durationMillis = 60),
        label = "OtpSlotElevation"
    )

    Surface(
        onClick = onClick,
        color = SolariTheme.colors.surface,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.5.dp, borderColor),
        shadowElevation = elevation,
        modifier = Modifier.size(48.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = value,
                color = SolariTheme.colors.onSurface,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = PlusJakartaSans,
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun focusedOtpSlotIndex(length: Int): Int {
    return length.coerceIn(0, 5)
}
