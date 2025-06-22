package com.example.myapplication.api

import com.google.gson.annotations.SerializedName

data class ErrorWithUserDto(
    @SerializedName("message") val message: String,
    @SerializedName("username") val username: String
)