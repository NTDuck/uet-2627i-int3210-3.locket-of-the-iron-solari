package com.solari.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solari.app.ui.components.SolariButton
import com.solari.app.ui.components.SolariBackButton
import com.solari.app.ui.components.SolariTextField
import com.solari.app.ui.theme.PlusJakartaSans
import com.solari.app.ui.theme.SolariTheme
import com.solari.app.ui.util.scaledClickable
import com.solari.app.ui.viewmodels.SignUpViewModel
import kotlinx.coroutines.delay

private val SignUpPrimaryContent = Color(0xFF5F2900)
private val SignUpFeedbackSurface = Color(0xFF421F1F)
private val SignUpFeedbackIcon = Color(0xFFFF8A80)

private enum class SignUpFocusedField {
    Username,
    Email,
    Password,
    ConfirmPassword
}

@Composable
fun SignUpScreen(
    viewModel: SignUpViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToSignIn: () -> Unit,
    onSignUpComplete: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    val density = LocalDensity.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val usernameFocusRequester = remember { FocusRequester() }
    val emailFocusRequester = remember { FocusRequester() }
    val passwordFocusRequester = remember { FocusRequester() }
    val confirmPasswordFocusRequester = remember { FocusRequester() }
    var feedbackPillVisible by remember { mutableStateOf(false) }
    var feedbackPillMessage by remember { mutableStateOf("") }
    var focusedField by remember { mutableStateOf<SignUpFocusedField?>(null) }
    var usernameBounds by remember { mutableStateOf<Rect?>(null) }
    var emailBounds by remember { mutableStateOf<Rect?>(null) }
    var passwordBounds by remember { mutableStateOf<Rect?>(null) }
    var confirmPasswordBounds by remember { mutableStateOf<Rect?>(null) }
    fun clearInputFocus() {
        focusedField = null
        focusManager.clearFocus(force = true)
    }
    fun isInsideInputField(position: Offset): Boolean {
        return listOfNotNull(
            usernameBounds,
            emailBounds,
            passwordBounds,
            confirmPasswordBounds
        ).any { bounds -> bounds.contains(position) }
    }
    val isKeyboardVisible = WindowInsets.ime.getBottom(density) > 0

    val contentOffset by animateDpAsState(
        targetValue = when (focusedField) {
            SignUpFocusedField.Password -> (-34).dp
            SignUpFocusedField.ConfirmPassword -> (-184).dp
            else -> 0.dp
        },
        animationSpec = tween(durationMillis = 180),
        label = "SignUpContentOffset"
    )

    LaunchedEffect(viewModel.errorMessage) {
        val message = viewModel.errorMessage ?: return@LaunchedEffect
        feedbackPillMessage = message
        feedbackPillVisible = true
        delay(1000)
        feedbackPillVisible = false
        delay(260)
        viewModel.clearMessages()
    }

    LaunchedEffect(isKeyboardVisible) {
        if (!isKeyboardVisible) {
            focusedField = null
            focusManager.clearFocus(force = true)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = SolariTheme.colors.background
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    awaitEachGesture {
                        awaitFirstDown(requireUnconsumed = false)
                        val up = waitForUpOrCancellation(pass = PointerEventPass.Final)
                        if (up != null && !isInsideInputField(up.position)) {
                            clearInputFocus()
                        }
                    }
                }
        ) {
            SolariBackButton(onClick = onNavigateBack)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .offset(y = contentOffset)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SolariTextField(
                    value = viewModel.username,
                    onValueChange = { viewModel.username = it },
                    label = "Username",
                    placeholder = "Username",
                    labelFontSize = 17.sp,
                    textFontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 24.dp),
                    color = SolariTheme.colors.tertiary,
                    textFieldModifier = Modifier
                        .focusRequester(usernameFocusRequester)
                        .onGloballyPositioned { usernameBounds = it.boundsInRoot() }
                        .onFocusChanged {
                            if (it.isFocused) focusedField = SignUpFocusedField.Username
                        },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(
                        onNext = { emailFocusRequester.requestFocus() }
                    )
                )

                SolariTextField(
                    value = viewModel.email,
                    onValueChange = { viewModel.email = it },
                    label = "Email Address",
                    placeholder = "Email address",
                    labelFontSize = 17.sp,
                    textFontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 24.dp),
                    color = SolariTheme.colors.tertiary,
                    textFieldModifier = Modifier
                        .focusRequester(emailFocusRequester)
                        .onGloballyPositioned { emailBounds = it.boundsInRoot() }
                        .onFocusChanged {
                            if (it.isFocused) focusedField = SignUpFocusedField.Email
                        },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(
                        onNext = { passwordFocusRequester.requestFocus() }
                    )
                )

                SolariTextField(
                    value = viewModel.password,
                    onValueChange = { viewModel.password = it },
                    label = "Password",
                    placeholder = "••••••••",
                    isPassword = true,
                    labelFontSize = 17.sp,
                    textFontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 24.dp),
                    color = SolariTheme.colors.tertiary,
                    textFieldModifier = Modifier
                        .focusRequester(passwordFocusRequester)
                        .onGloballyPositioned { passwordBounds = it.boundsInRoot() }
                        .onFocusChanged {
                            if (it.isFocused) focusedField = SignUpFocusedField.Password
                        },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(
                        onNext = { confirmPasswordFocusRequester.requestFocus() }
                    )
                )

                SolariTextField(
                    value = viewModel.confirmPassword,
                    onValueChange = { viewModel.confirmPassword = it },
                    label = "Confirm Password",
                    placeholder = "••••••••",
                    isPassword = true,
                    labelFontSize = 17.sp,
                    textFontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 40.dp),
                    color = SolariTheme.colors.tertiary,
                    textFieldModifier = Modifier
                        .focusRequester(confirmPasswordFocusRequester)
                        .onGloballyPositioned { confirmPasswordBounds = it.boundsInRoot() }
                        .onFocusChanged {
                            if (it.isFocused) focusedField = SignUpFocusedField.ConfirmPassword
                        }
                        .onPreviewKeyEvent { event ->
                            if (
                                (event.type == KeyEventType.KeyDown || event.type == KeyEventType.KeyUp) &&
                                event.key == Key.Enter
                            ) {
                                clearInputFocus()
                                keyboardController?.hide()
                                true
                            } else {
                                false
                            }
                        },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onNext = {
                            clearInputFocus()
                            keyboardController?.hide()
                        },
                        onDone = {
                            clearInputFocus()
                            keyboardController?.hide()
                        }
                    )
                )

                SolariButton(
                    text = if (viewModel.isSubmitting) "Creating..." else "Create Account",
                    onClick = {
                        clearInputFocus()
                        keyboardController?.hide()
                        viewModel.createAccount(onSuccess = onSignUpComplete)
                    },
                    containerColor = SolariTheme.colors.primary,
                    contentColor = SignUpPrimaryContent,
                    buttonHeight = 64.dp,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .padding(bottom = 16.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Already have an account? ",
                        color = SolariTheme.colors.tertiary,
                        fontSize = 16.sp,
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Log in",
                        color = SolariTheme.colors.primary,
                        fontSize = 16.sp,
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.scaledClickable(
                            pressedScale = 1.08f,
                            onClick = onNavigateToSignIn
                        )
                    )
                }
            }

            AnimatedVisibility(
                visible = feedbackPillVisible,
                enter = slideInVertically(
                    animationSpec = tween(durationMillis = 260),
                    initialOffsetY = { -it * 2 }
                ) + fadeIn(animationSpec = tween(durationMillis = 180)),
                exit = slideOutVertically(
                    animationSpec = tween(durationMillis = 220),
                    targetOffsetY = { -it * 2 }
                ) + fadeOut(animationSpec = tween(durationMillis = 160)),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 12.dp, start = 24.dp, end = 24.dp)
            ) {
                SignUpFeedbackPill(message = feedbackPillMessage)
            }

            if (viewModel.isSubmitting) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.42f))
                        .clickable(enabled = false) {},
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = SolariTheme.colors.primary,
                        trackColor = SolariTheme.colors.surface
                    )
                }
            }
        }
    }
}

@Composable
private fun SignUpFeedbackPill(message: String) {
    Surface(
        color = SignUpFeedbackSurface,
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 10.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                tint = SignUpFeedbackIcon,
                modifier = Modifier.size(22.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = message,
                color = Color.White,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
