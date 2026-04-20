package com.solari.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.solari.app.ui.theme.PlusJakartaSans
import com.solari.app.ui.theme.SolariTheme
import com.solari.app.ui.viewmodels.ProfileViewModel
import com.solari.app.ui.viewmodels.SettingsViewModel

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    settingsViewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToChangePassword: () -> Unit,
    onNavigateToChangeTheme: () -> Unit,
    onNavigateToManageFriends: () -> Unit,
    onNavigateToCamera: () -> Unit,
    onNavigateToFeed: () -> Unit,
    onNavigateToChat: () -> Unit,
    onLogout: () -> Unit
) {
    val user = viewModel.user
    val focusManager = LocalFocusManager.current
    val outsideEditClickSource = remember { MutableInteractionSource() }
    
    var editingField by remember { mutableStateOf<String?>(null) } // "displayName", "email"
    var tempValue by remember { mutableStateOf("") }
    
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var deletePassword by remember { mutableStateOf("") }
    var deleteError by remember { mutableStateOf<String?>(null) }

    if (user == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SolariTheme.colors.background),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = viewModel.errorMessage ?: "Loading profile",
                color = SolariTheme.colors.onBackground,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold
            )
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SolariTheme.colors.background)
            .clickable(
                enabled = editingField != null,
                interactionSource = outsideEditClickSource,
                indication = null
            ) {
                editingField = null
                focusManager.clearFocus()
            }
            .padding(horizontal = 24.dp),
        contentPadding = PaddingValues(top = 32.dp, bottom = 32.dp)
    ) {
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box {
                    AsyncImage(
                        model = user.profileImageUrl,
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .size(120.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .clickable { /* Select from gallery mock */ },
                        contentScale = ContentScale.Crop
                    )
                    IconButton(
                        onClick = { /* Edit Avatar */ },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = 8.dp, y = 8.dp)
                            .size(32.dp)
                            .background(SolariTheme.colors.primary, RoundedCornerShape(8.dp))
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Avatar", tint = SolariTheme.colors.onPrimary, modifier = Modifier.size(16.dp))
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
                    color = Color.Gray
                )
            }
        }

        item { Spacer(modifier = Modifier.height(32.dp)) }

        // Success/Error Messages
        if (viewModel.successMessage != null) {
            item {
                Text(viewModel.successMessage!!, color = Color.Green, fontFamily = PlusJakartaSans, fontSize = 14.sp, modifier = Modifier.padding(bottom = 8.dp))
            }
        }
        if (viewModel.errorMessage != null) {
            item {
                Text(viewModel.errorMessage!!, color = Color.Red, fontFamily = PlusJakartaSans, fontSize = 14.sp, modifier = Modifier.padding(bottom = 8.dp))
            }
        }

        // Username
        item {
            ProfileInfoBox(label = "USERNAME", value = "@${user.username}")
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }

        // Email
        item {
            if (editingField == "email") {
                EditableField(
                    label = "EMAIL",
                    value = tempValue,
                    onValueChange = { tempValue = it },
                    onDone = {
                        viewModel.updateEmail(tempValue)
                        editingField = null
                        focusManager.clearFocus()
                    }
                )
            } else {
                ProfileInfoBox(label = "EMAIL", value = user.email, onClick = {
                    tempValue = user.email
                    editingField = "email"
                    viewModel.clearMessages()
                })
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }

        // Display Name
        item {
            if (editingField == "displayName") {
                EditableField(
                    label = "DISPLAY NAME",
                    value = tempValue,
                    onValueChange = { tempValue = it },
                    onDone = {
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
                trailing = { Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray) }
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

        item {
            SettingsRow(
                icon = Icons.Default.Lock,
                title = "Change Password",
                onClick = onNavigateToChangePassword,
                trailing = { Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray) }
            )
        }

        item { Spacer(modifier = Modifier.height(8.dp)) }

        item {
            SettingsRow(
                icon = Icons.Default.Palette,
                title = "Change Theme",
                onClick = onNavigateToChangeTheme,
                trailing = { Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray) }
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
                icon = Icons.AutoMirrored.Filled.Logout,
                title = "Log Out",
                onClick = onLogout,
                trailing = { }
            )
        }

        item { Spacer(modifier = Modifier.height(8.dp)) }

        item {
            SettingsRow(
                icon = Icons.Default.Delete,
                title = "Delete Account",
                titleColor = Color(0xFFE57373),
                onClick = { showDeleteConfirm = true },
                trailing = { Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp)) }
            )
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Account", fontFamily = PlusJakartaSans) },
            text = {
                Column {
                    Text("This action cannot be undone. Please enter your password to confirm.", fontFamily = PlusJakartaSans)
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = deletePassword,
                        onValueChange = { deletePassword = it },
                        label = { Text("Password", fontFamily = PlusJakartaSans) },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(fontFamily = PlusJakartaSans)
                    )
                    if (deleteError != null) {
                        Text(deleteError!!, color = Color.Red, fontSize = 12.sp, fontFamily = PlusJakartaSans)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteAccount(
                        onSuccess = onLogout,
                        onFailure = { message -> deleteError = message }
                    )
                }) {
                    Text("Delete", color = Color.Red, fontFamily = PlusJakartaSans)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel", fontFamily = PlusJakartaSans)
                }
            }
        )
    }
}

@Composable
fun EditableField(label: String, value: String, onValueChange: (String) -> Unit, onDone: () -> Unit) {
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
            textStyle = TextStyle(fontFamily = PlusJakartaSans, fontSize = 16.sp, fontWeight = FontWeight.Medium),
            label = { Text(label, color = SolariTheme.colors.secondary, fontSize = 13.sp, fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onDone() }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = SolariTheme.colors.onSurface,
                unfocusedTextColor = SolariTheme.colors.onSurface,
                focusedBorderColor = SolariTheme.colors.primary,
                unfocusedBorderColor = Color.Gray
            )
        )

        Spacer(modifier = Modifier.width(12.dp))

        IconButton(
            onClick = onDone,
            modifier = Modifier
                .size(56.dp)
                .background(SolariTheme.colors.primary, RoundedCornerShape(12.dp))
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
fun ProfileInfoBox(label: String, value: String, onClick: (() -> Unit)? = null) {
    Surface(
        color = SolariTheme.colors.surface,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(label, color = SolariTheme.colors.secondary, fontSize = 13.sp, fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold)
                Text(value, color = SolariTheme.colors.onSurface, fontSize = 16.sp, fontFamily = PlusJakartaSans, fontWeight = FontWeight.Medium)
            }
            if (onClick != null) {
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
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
    Surface(
        color = SolariTheme.colors.surface,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = if (titleColor == null) SolariTheme.colors.primary else actualTitleColor)
                Spacer(modifier = Modifier.width(12.dp))
                Text(title, color = actualTitleColor, fontSize = 16.sp, fontFamily = PlusJakartaSans, fontWeight = FontWeight.Medium)
            }
            trailing()
        }
    }
}
