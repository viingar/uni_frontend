package com.example.myapplication.ViewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.api.ApiService
import com.example.myapplication.api.CodeResetRequest
import com.example.myapplication.api.EmailRequest
import com.example.myapplication.api.ResetPasswordRequest
import kotlinx.coroutines.launch

class PasswordResetViewModel(
    private val apiService: ApiService
) : ViewModel() {
    fun checkEmail(email: String, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val response = apiService.checkEmail(EmailRequest(email))
                callback(response.isSuccessful)
            } catch (_: Exception) {
                callback(false)
            }
        }
    }
    fun validateCode(email: String, code: String, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val response = apiService.validateCode(CodeResetRequest(email, code))
                callback(response.isSuccessful)
            } catch (_: Exception) {
                callback(false)
            }
        }
    }
    fun resetPassword(
        email: String,
        newPassword: String,
        confirmPassword: String,
        callback: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val response = apiService.resetPassword(
                    ResetPasswordRequest(email, newPassword, confirmPassword)
                )
                callback(response.isSuccessful)
            } catch (_: Exception) {
                callback(false)
            }
        }
    }
    fun resendCode(email: String) {
        viewModelScope.launch {
            try {
                apiService.checkEmail(EmailRequest(email))
            } catch (_: Exception) {

            }
        }
    }
    class PasswordResetViewModelFactory(
        private val apiService: ApiService
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PasswordResetViewModel(apiService) as T
        }
    }
}