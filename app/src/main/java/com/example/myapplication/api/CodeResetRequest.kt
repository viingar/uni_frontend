package com.example.myapplication.api

data class CodeResetRequest(
    val email: String,
    val code: String
)
