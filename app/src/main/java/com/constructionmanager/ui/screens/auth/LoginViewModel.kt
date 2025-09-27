package com.constructionmanager.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.constructionmanager.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val error: String? = null,
    val userEmail: String? = null,
    val userName: String? = null
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()
    
    init {
        checkAuthenticationStatus()
    }
    
    private fun checkAuthenticationStatus() {
        viewModelScope.launch {
            try {
                val isAuthenticated = authRepository.isAuthenticated()
                if (isAuthenticated) {
                    val user = authRepository.getCurrentUser()
                    _uiState.value = _uiState.value.copy(
                        isAuthenticated = true,
                        userEmail = user?.email,
                        userName = "${user?.firstName} ${user?.lastName}"
                    )
                }
            } catch (e: Exception) {
                // User not authenticated, continue with login screen
            }
        }
    }
    
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val user = authRepository.login(email, password)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isAuthenticated = true,
                    userEmail = user.email,
                    userName = "${user.firstName} ${user.lastName}"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Login failed. Please check your credentials."
                )
            }
        }
    }
    
    fun register(email: String, password: String, firstName: String, lastName: String, company: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val user = authRepository.register(email, password, firstName, lastName, company)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isAuthenticated = true,
                    userEmail = user.email,
                    userName = "${user.firstName} ${user.lastName}"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Registration failed. Please try again."
                )
            }
        }
    }
    
    fun loginAsDemo() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val user = authRepository.loginAsDemo()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isAuthenticated = true,
                    userEmail = user.email,
                    userName = "${user.firstName} ${user.lastName}"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Demo login failed. Please try again."
                )
            }
        }
    }
    
    fun logout() {
        viewModelScope.launch {
            try {
                authRepository.logout()
                _uiState.value = LoginUiState()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Logout failed"
                )
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}