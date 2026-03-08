package com.badgr.orbreader.ui.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.badgr.orbreader.sync.CloudSyncManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AccountUiState {
    object SignedOut                    : AccountUiState()
    object Loading                      : AccountUiState()
    data class SignedIn(val email: String) : AccountUiState()
    data class Error(val message: String)  : AccountUiState()
}

class AccountViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<AccountUiState>(
        if (CloudSyncManager.isSignedIn)
            AccountUiState.SignedIn(CloudSyncManager.currentUser?.email ?: "")
        else
            AccountUiState.SignedOut
    )
    val uiState: StateFlow<AccountUiState> = _uiState.asStateFlow()

    fun signIn(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = AccountUiState.Error("Email and password are required.")
            return
        }
        viewModelScope.launch {
            _uiState.value = AccountUiState.Loading
            try {
                val user = CloudSyncManager.signIn(email.trim(), password)
                _uiState.value = AccountUiState.SignedIn(user.email ?: "")
            } catch (e: Exception) {
                _uiState.value = AccountUiState.Error(
                    e.localizedMessage ?: "Sign-in failed. Check your credentials."
                )
            }
        }
    }

    fun signUp(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = AccountUiState.Error("Email and password are required.")
            return
        }
        if (password.length < 6) {
            _uiState.value = AccountUiState.Error("Password must be at least 6 characters.")
            return
        }
        viewModelScope.launch {
            _uiState.value = AccountUiState.Loading
            try {
                val user = CloudSyncManager.signUp(email.trim(), password)
                _uiState.value = AccountUiState.SignedIn(user.email ?: "")
            } catch (e: Exception) {
                _uiState.value = AccountUiState.Error(
                    e.localizedMessage ?: "Sign-up failed. Email may already be in use."
                )
            }
        }
    }

    fun signOut() {
        CloudSyncManager.signOut()
        _uiState.value = AccountUiState.SignedOut
    }

    fun clearError() {
        _uiState.value = AccountUiState.SignedOut
    }
}
