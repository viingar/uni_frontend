package com.example.myapplication.ViewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.api.AuthRequest
import com.example.myapplication.api.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RegisterViewModel : ViewModel() {
    private val _registrationSuccess = MutableStateFlow(false)
    val registrationSuccess: StateFlow<Boolean> = _registrationSuccess.asStateFlow()

    fun registerUser(username: String, email: String, password: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.registerUser(
                    AuthRequest(username, email, password)
                )
                _registrationSuccess.value = response.isSuccessful
            } catch (_: Exception) {

            }
        }
    }
}