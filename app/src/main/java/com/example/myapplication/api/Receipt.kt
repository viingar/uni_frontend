package com.example.myapplication.api

data class Receipt(
    val id: String,
    val storeName: String,
    val totalSum: String,
    val dateTime: String,
    val items: List<ReceiptItem>? = null
)