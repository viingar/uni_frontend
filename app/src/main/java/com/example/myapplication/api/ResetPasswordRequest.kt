package com.example.myapplication.api

data class ResetPasswordRequest(
    val email: String,
    val newPassword: String,
    val confirmPassword: String
)
