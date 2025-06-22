package com.example.myapplication.ViewModels
import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.api.ErrorReportRequest
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

    private val _passwordChangeSuccess = MutableStateFlow(false)
    val passwordChangeSuccess: StateFlow<Boolean> = _passwordChangeSuccess.asStateFlow()

    private val _passwordChangeError = MutableStateFlow<String?>(null)
    val passwordChangeError: StateFlow<String?> = _passwordChangeError.asStateFlow()

    private val _profileImageUri = MutableStateFlow<Uri?>(null)
    val profileImageUri: StateFlow<Uri?> = _profileImageUri.asStateFlow()

    private val _imageUploadSuccess = MutableStateFlow(false)
    val imageUploadSuccess: StateFlow<Boolean> = _imageUploadSuccess.asStateFlow()

    private val _imageUploadError = MutableStateFlow<String?>(null)
    val imageUploadError: StateFlow<String?> = _imageUploadError.asStateFlow()

    fun logout() {
        viewModelScope.launch {
            try {
                val response = secureApiService.logout()
                if (response.isSuccessful) {
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
                    println("Ошибка отправлена")
                }
            } catch (_: Exception) {

            }
        }
    }

    fun changePassword(currentPassword: String, newPassword: String, confirmPassword: String) {
        viewModelScope.launch {
            try {
                _loading.value = true
                _passwordChangeError.value = null
                _passwordChangeSuccess.value = false

                if (newPassword != confirmPassword) {
                    _passwordChangeError.value = "Пароли не совпадают"
                    return@launch
                }

                if (newPassword.length < 6) {
                    _passwordChangeError.value = "Новый пароль должен содержать не менее 6 символов"
                    return@launch
                }

                _passwordChangeSuccess.value = true
                
            } catch (e: Exception) {
                _passwordChangeError.value = "Ошибка изменения пароля: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun uploadProfileImage(uri: Uri) {
        viewModelScope.launch {
            try {
                _loading.value = true
                _imageUploadError.value = null
                _imageUploadSuccess.value = false
                _profileImageUri.value = uri
                _imageUploadSuccess.value = true

            } catch (e: Exception) {
                _imageUploadError.value = "Ошибка загрузки фото: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun clearPasswordChangeSuccess() {
        _passwordChangeSuccess.value = false
    }

    fun clearPasswordChangeError() {
        _passwordChangeError.value = null
    }

    fun clearImageUploadSuccess() {
        _imageUploadSuccess.value = false
    }

    fun clearImageUploadError() {
        _imageUploadError.value = null
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