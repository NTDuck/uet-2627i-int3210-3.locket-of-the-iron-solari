package com.solari.app.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class PasswordResetViewModel : ViewModel() {
    var newPassword by mutableStateOf("")
    var confirmPassword by mutableStateOf("")
}
