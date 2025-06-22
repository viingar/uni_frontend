package com.example.myapplication.api

import android.content.Context
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
  private const val BASE_URL = "http://5.129.195.76:8070/"
 //   private const val BASE_URL = "http://10.0.2.2:8070/"
    val apiService: ApiService by lazy {
        createRetrofit().create(ApiService::class.java)
    }

    fun getSecureApiService(context: Context): SecureApiService {
        return createRetrofitWithAuth(context).create(SecureApiService::class.java)
    }

    private fun createRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private fun createRetrofitWithAuth(context: Context): Retrofit {
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val originalRequest = chain.request()
                val token = getTokenFromSharedPrefs(context)

                if (token.isNullOrBlank()) {
                    chain.proceed(originalRequest)
                } else {
                    val requestWithToken = originalRequest.newBuilder()
                        .header("Authorization", "Bearer $token")
                        .build()
                    chain.proceed(requestWithToken)
                }
            }
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private fun getTokenFromSharedPrefs(context: Context): String? {
        val sharedPref = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
        return sharedPref.getString("access_token", null)
    }
}