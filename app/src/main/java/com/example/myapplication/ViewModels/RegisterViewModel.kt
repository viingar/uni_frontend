package com.example.myapplication.ViewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.api.AuthRequest
import com.example.myapplication.api.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class RegisterViewModel : ViewModel() {
    private val _registrationSuccess = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)
    private val _isLoading = MutableStateFlow(false)

    val registrationSuccess: StateFlow<Boolean> = _registrationSuccess.asStateFlow()
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun registerUser(username: String, email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _registrationSuccess.value = false
            
            try {
                Log.d("RegisterViewModel", "Attempting registration for username: $username, email: $email")
                val response = RetrofitClient.apiService.registerUser(
                    AuthRequest(username, email, password)
                )

                Log.d("RegisterViewModel", "Response code: ${response.code()}, isSuccessful: ${response.isSuccessful}")

                if (response.isSuccessful) {
                    Log.d("RegisterViewModel", "Registration successful")
                    _registrationSuccess.value = true
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.d("RegisterViewModel", "Error body: $errorBody")
                    
                    if (errorBody != null) {
                        try {
                            val jsonObject = JSONObject(errorBody)
                            val errorMsg = jsonObject.getString("message")
                            _errorMessage.value = errorMsg
                        } catch (e: Exception) {
                            _errorMessage.value = "Ошибка регистрации: $errorBody"
                        }
                    } else {
                        _errorMessage.value = "Ошибка регистрации: ${response.message()}"
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = when {
                    e is UnknownHostException || e is SocketTimeoutException ->
                        "Нет подключения к интернету"
                    else -> "Произошла ошибка: ${e.localizedMessage}"
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}
