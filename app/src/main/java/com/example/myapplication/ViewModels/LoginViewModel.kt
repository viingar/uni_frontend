package com.example.myapplication.ViewModels

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.api.ApiService
import com.example.myapplication.api.LoginRequest
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LoginViewModel(
    private val apiService: ApiService,
    private val context: Context
) : ViewModel() {
    private val _loginSuccess = MutableStateFlow(false)
    val loginSuccess: StateFlow<Boolean> = _loginSuccess.asStateFlow()

    private val _userRole = MutableStateFlow<String?>(null)
    val userRole: StateFlow<String?> = _userRole.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    companion object {
        private const val TAG = "LoginViewModel"
    }

    fun loginUser(username: String, password: String) {
        viewModelScope.launch {
            try {
                _errorMessage.value = null
                val response = apiService.loginUser(LoginRequest(username, password))
                if (response.isSuccessful) {
                    response.body()?.let { loginResponse ->
                        if (loginResponse.accessToken.isNotEmpty()) {
                            saveToken(loginResponse.accessToken)
                            _userRole.value = loginResponse.role
                            _loginSuccess.value = true
                        } else {
                            _errorMessage.value = "Access token is empty"
                            Log.e(TAG, "Access token is empty in response")
                        }
                    } ?: run {
                        _errorMessage.value = "Response body is null"
                        Log.e(TAG, "Response body is null")
                    }
                } else {
                    val error = response.errorBody()?.string() ?: "Unknown error"
                    val errorMessage = when (response.code()) {
                        404 -> "Такого пользователя не существует"
                        401 -> "Неверный логин или пароль"
                        400 -> "Неверный формат данных"
                        500 -> "Ошибка сервера"
                        else -> "Ошибка входа: $error"
                    }
                    _errorMessage.value = errorMessage
                    Log.e(TAG, "Login failed: $error")
                }
            } catch (e: Exception) {
                val errorMessage = when {
                    e.message?.contains("Unable to resolve host") == true -> "Ошибка сети. Проверьте подключение к интернету"
                    e.message?.contains("timeout") == true -> "Превышено время ожидания. Попробуйте еще раз"
                    else -> "Ошибка сети: ${e.message}"
                }
                _errorMessage.value = errorMessage
                Log.e(TAG, "Network error: ${e.message}", e)
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    private fun saveToken(token: String) {
        if (token.isBlank()) {
            Log.e(TAG, "Attempt to save empty token")
            return
        }

        try {
            val sharedPref = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
            with(sharedPref.edit()) {
                putString("access_token", token)
                apply()
            }
            Log.d(TAG, "Token saved successfully (length: ${token.length} chars)")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save token: ${e.message}", e)
        }
    }
    class LoginViewModelFactory(
        private val apiService: ApiService,
        private val context: Context
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
                return LoginViewModel(apiService, context) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
