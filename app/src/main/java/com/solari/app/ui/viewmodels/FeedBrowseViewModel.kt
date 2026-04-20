package com.solari.app.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.solari.app.ui.models.User

class FeedBrowseViewModel : ViewModel() {
    var friends by mutableStateOf(
        listOf(
            User("2", "Alice", "alice", "alice@solari.app"),
            User("3", "Benjamin", "ben", "ben@solari.app"),
            User("4", "Charlie", "charlie", "charlie@solari.app"),
            User("1", "John", "john_n", "john@solari.app")
        )
    )
        private set

    var browseImages by mutableStateOf(List(15) { "https://www.politicon.com/wp-content/uploads/2017/06/Charlie-Kirk-2019-1024x1024.jpg" })
        private set
}
