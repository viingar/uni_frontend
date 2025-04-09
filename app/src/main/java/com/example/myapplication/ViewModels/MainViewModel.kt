package com.example.myapplication.ViewModels

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.api.Receipt
import com.example.myapplication.api.ReceiptData
import com.example.myapplication.api.RetrofitClient
import com.example.myapplication.api.SecureApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(
    private val secureApiService: SecureApiService,
    private val context: Context
) : ViewModel() {
    private val _receipts = mutableStateListOf<Receipt>()
    val receipts: List<Receipt> = _receipts

    private val _showDialog = MutableStateFlow(false)
    val showDialog: StateFlow<Boolean> = _showDialog.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)

    fun showAddManualDialog() {
        _showDialog.value = true
    }

    fun hideDialog() {
        _showDialog.value = false
    }

    fun loadReceipts() {
        viewModelScope.launch {
            try {
                val response = secureApiService.getReceipts()
                if (response.isSuccessful) {
                    _receipts.clear()
                    response.body()?.let { _receipts.addAll(it) }
                } else if (response.code() == 401) {
                    _errorMessage.value = "Session expired. Please login again."
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load receipts: ${e.message}"
            }
        }
    }

    fun addReceipt(data: ReceiptData) {
        viewModelScope.launch {
            try {
                val response = secureApiService.addReceipt(data)
                if (response.isSuccessful) {
                    response.body()?.let { _receipts.add(0, it) }
                } else if (response.code() == 401) {
                    _errorMessage.value = "Session expired. Please login again."
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to add receipt: ${e.message}"
            }
        }
    }
    class MainViewModelFactory(
        private val context: Context
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                return MainViewModel(
                    RetrofitClient.getSecureApiService(context),
                    context
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}