package com.example.myapplication.ViewModels

import android.content.Context
import android.util.Log
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

    private val loadedReceiptKeys = mutableSetOf<String>()

    private val _showDialog = MutableStateFlow(false)
    val showDialog: StateFlow<Boolean> = _showDialog.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _duplicateReceiptError = MutableStateFlow<String?>(null)
    val duplicateReceiptError: StateFlow<String?> = _duplicateReceiptError.asStateFlow()

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
                    loadedReceiptKeys.clear()
                    response.body()?.let { receipts ->
                        _receipts.addAll(receipts)

                    }
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

                val receiptKey = "${data.fn}_${data.i}_${data.fp}"

                if (loadedReceiptKeys.contains(receiptKey)) {

                    _duplicateReceiptError.value = "Такой чек уже загружен в систему"
                    return@launch
                }

                Log.d("MainViewModel", "Отправляем чек на сервер: $receiptKey")
                val response = secureApiService.addReceipt(data)
                if (response.isSuccessful) {
                    response.body()?.let { receipt ->
                        _receipts.add(0, receipt)
                        loadedReceiptKeys.add(receiptKey)
                    }
                    _duplicateReceiptError.value = null
                } else if (response.code() == 401) {
                    _errorMessage.value = "Session expired. Please login again."
                } else if (response.code() == 409) {

                    _duplicateReceiptError.value = "Такой чек уже загружен в систему"
                } else {
                    _errorMessage.value = "Failed to add receipt: ${response.code()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to add receipt: ${e.message}"
            }
        }
    }

    fun clearDuplicateError() {
        Log.d("MainViewModel", "Очищаем ошибку дублирования")
        _duplicateReceiptError.value = null
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