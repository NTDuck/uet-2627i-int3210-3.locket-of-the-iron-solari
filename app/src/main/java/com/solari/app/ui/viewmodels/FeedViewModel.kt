package com.solari.app.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.solari.app.ui.models.Post
import com.solari.app.ui.models.User

class FeedViewModel : ViewModel() {
    private val currentUser = User(
        id = "1",
        displayName = "John Netanyahu",
        username = "john_netanyahu",
        email = "bnetanyahu@knesset.gov.il"
    )

    var posts by mutableStateOf(
        listOf(
            Post(
                id = "1",
                author = currentUser,
                timestamp = System.currentTimeMillis() - 7200000 // 2 hours ago
            ),
            Post(
                id = "2",
                author = User(id = "2", displayName = "Sara Netanyahu", username = "sara_n", email = "sara@gov.il"),
                timestamp = System.currentTimeMillis() - 14400000 // 4 hours ago
            )
        )
    )
        private set

    fun deletePost(postId: String) {
        posts = posts.filter { it.id != postId }
    }
}
