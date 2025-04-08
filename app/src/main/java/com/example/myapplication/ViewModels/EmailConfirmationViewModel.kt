package com.example.myapplication.ViewModels
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.api.VerifyRequest
import com.example.myapplication.api.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EmailConfirmationViewModel(
    private val email: String
) : ViewModel() {
    private val _verificationSuccess = MutableStateFlow(false)
    val verificationSuccess: StateFlow<Boolean> = _verificationSuccess.asStateFlow()
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun verifyEmail(code: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val response = RetrofitClient.apiService.verifyEmail(
                    VerifyRequest(email = email, verificationCode = code) // Используем this.email
                )
                _verificationSuccess.value = response.isSuccessful
                if (!response.isSuccessful) {
                    _errorMessage.value = "Verification failed"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Network error"
            } finally {
                _isLoading.value = false
            }
        }
    }
}