package com.solari.app.ui.screens

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.solari.app.ui.viewmodels.ChatSettingsViewModel

@Composable
fun ChatSettingsScreen(
    viewModel: ChatSettingsViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToCamera: () -> Unit,
    onNavigateToFeed: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    var notificationsEnabled by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AsyncImage(
                    model = "https://picsum.photos/seed/friend/200/200",
                    contentDescription = "Friend Avatar",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Aris Thorne",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "@aris_t",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

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
                        checked = notificationsEnabled,
                        onCheckedChange = { notificationsEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

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
                trailing = {
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            SettingsRow(
                icon = Icons.Default.Block,
                title = "Block User",
                titleColor = Color(0xFFE57373),
                trailing = {
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
                }
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
