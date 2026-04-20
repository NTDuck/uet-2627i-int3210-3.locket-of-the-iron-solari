package com.solari.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.solari.app.data.ServiceLocator
import com.solari.app.navigation.SolariRoute
import com.solari.app.ui.components.SolariBottomNavBar
import com.solari.app.ui.models.Message
import com.solari.app.ui.models.User
import com.solari.app.ui.theme.PlusJakartaSans
import com.solari.app.ui.viewmodels.ChatViewModel
import java.util.Calendar

private val ChatBackground = Color(0xFF111316)
private val ChatHeader = Color(0xFF1B1C21)
private val ChatIncomingBubble = Color(0xFF1D1E23)
private val ChatOutgoingBubble = Color(0xFF34363B)
private val ChatInput = Color(0xFF080B0E)
private val ChatPrimary = Color(0xFFFF8426)
private val ChatText = Color(0xFFE3E2E6)
private val ChatMuted = Color(0xFF9699A1)
private val ChatChip = Color(0xFF080B0E)
private val ChatReadText = Color(0xFFE8D6CB)

private data class ChatMessageBlock(
    val senderId: String,
    val messages: List<Message>
)

@Composable
fun ChatScreen(
    chatId: String,
    viewModel: ChatViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToCamera: () -> Unit,
    onNavigateToFeed: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val currentUser = ServiceLocator.mockDataProvider.currentUser
    val partner = viewModel.conversation?.otherUser ?: User(
        id = "partner",
        displayName = "John Netanyahu",
        username = "john_netanyahu",
        email = "john@example.com"
    )
    val focusManager = LocalFocusManager.current
    val mockMessages = remember(chatId, currentUser.id, partner.id) {
        buildMockChatMessages(currentUserId = currentUser.id, partnerId = partner.id)
    }
    var localMessages by remember(chatId) { mutableStateOf<List<Message>>(emptyList()) }
    val allMessages = mockMessages + localMessages
    val messageBlocks = remember(allMessages) { groupConsecutiveMessages(allMessages) }
    val lastMessage = allMessages.lastOrNull()

    Scaffold(
        containerColor = ChatBackground,
        bottomBar = {
            SolariBottomNavBar(
                selectedRoute = SolariRoute.Screen.Chat.name,
                onNavigate = { route ->
                    when (route) {
                        SolariRoute.Screen.CameraBefore.name -> onNavigateToCamera()
                        SolariRoute.Screen.Feed.name -> onNavigateToFeed()
                        SolariRoute.Screen.Profile.name -> onNavigateToProfile()
                        SolariRoute.Screen.Conversations.name -> onNavigateBack()
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(ChatBackground)
                .padding(innerPadding)
        ) {
            ChatHeaderBar(
                partner = partner,
                onNavigateBack = onNavigateBack,
                onNavigateToSettings = onNavigateToSettings
            )

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 24.dp)
            ) {
                item {
                    ChatDayChip(text = "TODAY")
                }

                items(messageBlocks) { block ->
                    val isFromMe = block.senderId == currentUser.id
                    val lastBlockMessage = block.messages.last()

                    ChatMessageBlockRow(
                        block = block,
                        partner = partner,
                        isFromMe = isFromMe,
                        isLastBlock = lastMessage?.id == lastBlockMessage.id,
                        showReadReceipt = isFromMe &&
                                lastMessage?.id == lastBlockMessage.id &&
                                lastBlockMessage.isRead
                    )
                }
            }

            ChatInputBar(
                value = viewModel.messageText,
                onValueChange = { viewModel.messageText = it },
                onSend = {
                    val trimmedMessage = viewModel.messageText.trim()
                    if (trimmedMessage.isNotEmpty()) {
                        localMessages = localMessages + Message(
                            senderId = currentUser.id,
                            text = trimmedMessage,
                            timestamp = System.currentTimeMillis(),
                            isRead = true
                        )
                        viewModel.messageText = ""
                        focusManager.clearFocus()
                    }
                }
            )
        }
    }
}

@Composable
private fun ChatHeaderBar(
    partner: User,
    onNavigateBack: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(ChatHeader)
            .padding(top = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = ChatPrimary,
                modifier = Modifier
                    .size(26.dp)
                    .clickable(onClick = onNavigateBack)
                    .clip(RoundedCornerShape(4.dp))
            )

            Spacer(modifier = Modifier.width(20.dp))

            AsyncImage(
                model = partner.profileImageUrl,
                contentDescription = "${partner.displayName} avatar",
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = partner.displayName,
                color = ChatText,
                fontSize = 20.sp,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = "Chat settings",
                tint = ChatMuted,
                modifier = Modifier
                    .size(24.dp)
                    .clickable(onClick = onNavigateToSettings)
            )
        }
    }
}

@Composable
private fun ChatDayChip(text: String) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .background(ChatChip)
                .padding(horizontal = 18.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = ChatReadText,
                fontSize = 12.sp,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ChatMessageBlockRow(
    block: ChatMessageBlock,
    partner: User,
    isFromMe: Boolean,
    isLastBlock: Boolean,
    showReadReceipt: Boolean
) {
    val lastMessage = block.messages.last()

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isFromMe) Alignment.End else Alignment.Start
    ) {
        if (isFromMe) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                block.messages.forEach { message ->
                    ChatBubble(
                        message = message,
                        isFromMe = true
                    )
                }
            }

            if (isLastBlock) {
                Text(
                    text = if (showReadReceipt) {
                        "Read 09:50 AM"
                    } else {
                        "09:50 AM"
                    },
                    color = ChatReadText,
                    fontSize = 13.sp,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 6.dp, end = 2.dp, bottom = 8.dp)
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = partner.profileImageUrl,
                    contentDescription = "${partner.displayName} avatar",
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(8.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    block.messages.forEach { message ->
                        ChatBubble(
                            message = message,
                            isFromMe = false
                        )
                    }
                }
            }

            if (isLastBlock) {
                Text(
                    text = "09:48 AM",
                    color = ChatReadText,
                    fontSize = 13.sp,
                    fontFamily = PlusJakartaSans,
                    modifier = Modifier.padding(top = 2.dp, bottom = 10.dp, start = 44.dp)
                )
            }
        }
    }
}

@Composable
private fun ChatBubble(
    message: Message,
    isFromMe: Boolean
) {
    Box(
        modifier = Modifier
            .widthIn(max = if (isFromMe) 292.dp else 248.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (isFromMe) ChatOutgoingBubble else ChatIncomingBubble)
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Text(
            text = message.text,
            color = ChatText,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.Normal
        )
    }
}

@Composable
private fun ChatInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .height(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(ChatInput)
            .padding(start = 20.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            textStyle = TextStyle(
                color = ChatText,
                fontSize = 15.sp,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Medium
            ),
            cursorBrush = SolidColor(ChatText),
            singleLine = true,
            decorationBox = { innerTextField ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if (value.isEmpty()) {
                        Text(
                            text = "Lorem ipsum dolor sit amet",
                            color = ChatMuted,
                            fontSize = 14.sp,
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    innerTextField()
                }
            }
        )

        Spacer(modifier = Modifier.width(12.dp))

        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(ChatPrimary)
                .clickable(onClick = onSend),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "Send message",
                tint = Color(0xFF5F2900),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

private fun buildMockChatMessages(
    currentUserId: String,
    partnerId: String
): List<Message> {
    val today = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 9)
        set(Calendar.MINUTE, 35)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    fun minutesAfter(minutes: Int): Long {
        return today.timeInMillis + minutes * 60_000L
    }

    return listOf(
        Message(
            senderId = partnerId,
            text = "Lorem ipsum dolor sit amet,\nconsectetur adipiscing elit,\nsed do eiusmod tempor\nincididunt ut labore et\ndolore magna aliqua.",
            timestamp = minutesAfter(1)
        ),
        Message(
            senderId = partnerId,
            text = "Lorem ipsum dolor sit\namet, consectetur.",
            timestamp = minutesAfter(6)
        ),
        Message(
            senderId = partnerId,
            text = "Sed ut perspiciatis unde\nomnis iste natus error.",
            timestamp = minutesAfter(7)
        ),
        Message(
            senderId = currentUserId,
            text = "Lorem ipsum dolor sit amet,\nconsectetur adipiscing elit, sed\ndo eiusmod tempor incididunt.",
            timestamp = minutesAfter(8)
        ),
        Message(
            senderId = currentUserId,
            text = "Lorem ipsum dolor sit amet,\nconsectetur adipiscing elit, sed\ndo eiusmod tempor incididunt ut\nlabore et dolore magna aliqua.",
            timestamp = minutesAfter(9),
            isRead = true
        ),
        Message(
            senderId = partnerId,
            text = "Lorem ipsum dolor sit amet,\nsed do eiusmod tempor.",
            timestamp = minutesAfter(11)
        ),
        Message(
            senderId = currentUserId,
            text = "Consectetur adipiscing elit,\nsed do eiusmod.",
            timestamp = minutesAfter(12)
        ),
        Message(
            senderId = partnerId,
            text = "Dolore magna aliqua.",
            timestamp = minutesAfter(13)
        ),
        Message(
            senderId = currentUserId,
            text = "Ut enim ad minim veniam,\nquis nostrud exercitation.",
            timestamp = minutesAfter(14)
        ),
        Message(
            senderId = currentUserId,
            text = "Duis aute irure dolor in\nreprehenderit.",
            timestamp = minutesAfter(15),
            isRead = true
        )
    )
}

private fun groupConsecutiveMessages(messages: List<Message>): List<ChatMessageBlock> {
    if (messages.isEmpty()) return emptyList()

    val blocks = mutableListOf<ChatMessageBlock>()
    var currentSenderId = messages.first().senderId
    val currentMessages = mutableListOf<Message>()

    messages.forEach { message ->
        if (message.senderId != currentSenderId) {
            blocks += ChatMessageBlock(
                senderId = currentSenderId,
                messages = currentMessages.toList()
            )
            currentMessages.clear()
            currentSenderId = message.senderId
        }
        currentMessages += message
    }

    blocks += ChatMessageBlock(
        senderId = currentSenderId,
        messages = currentMessages.toList()
    )

    return blocks
}
