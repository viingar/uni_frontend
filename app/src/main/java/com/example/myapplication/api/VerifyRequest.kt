package com.example.myapplication.api

data class VerifyRequest(
    val email: String,
    val verificationCode: String
)
