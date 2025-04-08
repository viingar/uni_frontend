package com.example.myapplication.ViewModels
import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.api.ErrorReportRequest
import com.example.myapplication.api.Receipt
import com.example.myapplication.api.ReceiptData
import com.example.myapplication.api.RetrofitClient
import com.example.myapplication.api.SecureApiService
import com.example.myapplication.api.UserInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val secureApiService: SecureApiService,
    private val context: Context
) : ViewModel() {
    private val _userInfo = MutableStateFlow<UserInfo?>(null)
    val userInfo: StateFlow<UserInfo?> = _userInfo.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _logoutSuccess = MutableStateFlow(false)
    val logoutSuccess: StateFlow<Boolean> = _logoutSuccess.asStateFlow()

    fun logout() {
        viewModelScope.launch {
            try {
                val response = secureApiService.logout()
                if (response.isSuccessful) {
                    // Очищаем токен из SharedPreferences
                    clearToken()
                    _logoutSuccess.value = true
                } else {
                    _errorMessage.value = "Logout failed: ${response.errorBody()?.string()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Network error: ${e.message}"
            }
        }
    }

    private fun clearToken() {
        val sharedPref = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
        sharedPref.edit().remove("access_token").apply()
    }

    fun loadUserInfo() {
        viewModelScope.launch {
            try {
                _loading.value = true
                _errorMessage.value = null

                val response = secureApiService.getUserInfo()
                if (response.isSuccessful) {
                    response.body()?.let { userInfoResponse ->
                        _userInfo.value = UserInfo(
                            email = userInfoResponse.email,
                            username = userInfoResponse.username
                        )
                    } ?: run {
                        _errorMessage.value = "Failed to load user info"
                    }
                } else if (response.code() == 401) {
                    _errorMessage.value = "Session expired. Please login again."
                } else {
                    _errorMessage.value = "Error: ${response.errorBody()?.string() ?: "Unknown error"}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Network error: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }
    fun reportError(message: String) {
        viewModelScope.launch {
            try {
                val response = secureApiService.reportError(ErrorReportRequest(message))
                if (!response.isSuccessful) {
                    // Обработка ошибки
                }
            } catch (e: Exception) {
                // Обработка исключения
            }
        }
    }
    class ProfileViewModelFactory(
        private val context: Context
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
                return ProfileViewModel(
                    RetrofitClient.getSecureApiService(context),
                    context
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}