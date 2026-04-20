package com.solari.app.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.solari.app.ui.models.User

class HomepageAfterCapturingViewModel : ViewModel() {
    var friends by mutableStateOf(
        listOf(
            User("2", "Alice", "alice", "alice@solari.app"),
            User("3", "Benjamin", "ben", "ben@solari.app"),
            User("4", "Charlie", "charlie", "charlie@solari.app"),
            User("1", "John", "john_n", "john@solari.app")
        )
    )
        private set
}
