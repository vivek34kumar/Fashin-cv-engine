package com.example.data.remote

import com.example.data.local.entity.ResearchSourceEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class ResearchSourceSync {
  private val client = OkHttpClient.Builder()
    .connectTimeout(15, TimeUnit.SECONDS)
    .readTimeout(20, TimeUnit.SECONDS)
    .build()

  suspend fun testSourceConnection(source: ResearchSourceEntity): ApiResult<String> = withContext(Dispatchers.IO) {
    if (source.urlOrEndpoint.isBlank()) {
      return@withContext ApiResult.Error("Source URL is empty.")
    }

    try {
      val requestBuilder = Request.Builder()
        .url(source.urlOrEndpoint.trim())

      if (!source.apiKey.isNullOrBlank()) {
        requestBuilder.header("Authorization", "Bearer ${source.apiKey.trim()}")
      }

      val request = requestBuilder.get().build()

      client.newCall(request).execute().use { response ->
        if (response.isSuccessful || response.code in 200..399) {
          ApiResult.Success("Source reachable. Status: HTTP ${response.code}")
        } else {
          ApiResult.Error("Source responded with HTTP ${response.code}: ${response.message}", response.code)
        }
      }
    } catch (e: Exception) {
      ApiResult.Error(e.localizedMessage ?: "Could not connect to research source.")
    }
  }
}
