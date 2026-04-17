package com.badgr.orbreader.data.remote

import com.badgr.orbreader.config.ApiConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    private val okHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)   // PDF/EPUB conversion can be slow
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    val convertApi: ConvertApi by lazy {
        // Ensure BASE_URL ends with a slash – Retrofit requires it.
        val baseUrl = ApiConfig.BASE_URL.trimEnd('/') + "/"

        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ConvertApi::class.java)
    }
}
