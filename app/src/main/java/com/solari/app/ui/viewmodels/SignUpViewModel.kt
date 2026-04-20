package com.solari.app.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class SignUpViewModel : ViewModel() {
    var username by mutableStateOf("")
    var email by mutableStateOf("")
    var displayName by mutableStateOf("")
    var password by mutableStateOf("")
    var confirmPassword by mutableStateOf("")
}
