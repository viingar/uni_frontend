package com.example.myapplication.api

data class AuthRequest(
    val username: String,
    val email: String,
    val password: String
)