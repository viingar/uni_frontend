package com.example.myapplication.api

import com.google.gson.annotations.SerializedName

data class UserInfoResponse(
    @SerializedName("email") val email: String,
    @SerializedName("username") val username: String
)
