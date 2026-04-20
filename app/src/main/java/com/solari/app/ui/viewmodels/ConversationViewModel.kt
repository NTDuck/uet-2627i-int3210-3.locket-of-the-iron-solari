package com.solari.app.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.solari.app.ui.models.Conversation
import com.solari.app.ui.models.FriendRequest
import com.solari.app.ui.models.User

class ConversationViewModel : ViewModel() {
    var friendRequests by mutableStateOf(
        listOf(
            FriendRequest("1", User("2", "Julian Dax", "dax_solari", "julian@solari.app")),
            FriendRequest("2", User("3", "Mina Kova", "kovacreates", "mina@solari.app"))
        )
    )
        private set

    var conversations by mutableStateOf(
        listOf(
            Conversation("1", User("4", "Aris Thorne", "aris_t", "aris@solari.app"), "The prototype for the orbital stati...", System.currentTimeMillis() - 120000, true),
            Conversation("2", User("5", "Elias Lowen", "elias_l", "elias@solari.app"), "Let's coordinate the deployment f...", System.currentTimeMillis() - 3600000, false),
            Conversation("3", User("6", "Sera Rin", "sera_r", "sera@solari.app"), "I've attached the latest encrypted l...", System.currentTimeMillis() - 86400000, false)
        )
    )
        private set

    fun acceptFriendRequest(requestId: String) {
        friendRequests = friendRequests.filter { it.id != requestId }
    }

    fun declineFriendRequest(requestId: String) {
        friendRequests = friendRequests.filter { it.id != requestId }
    }
}
