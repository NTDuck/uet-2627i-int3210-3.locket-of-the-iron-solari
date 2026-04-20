package com.solari.app.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class PasswordRecoveryViewModel : ViewModel() {
    var emailOrUsername by mutableStateOf("")
}
