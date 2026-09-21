package com.example.data.remote

import com.example.data.local.entity.PublishingDestinationEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class PublishingApiClient {
  private val client = OkHttpClient.Builder()
    .connectTimeout(20, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .build()

  suspend fun testDestination(destination: PublishingDestinationEntity): ApiResult<String> = withContext(Dispatchers.IO) {
    if (destination.endpoint.isBlank()) {
      return@withContext ApiResult.Error("Publishing endpoint URL is empty.")
    }

    try {
      val requestBuilder = Request.Builder()
        .url(destination.endpoint.trim())

      if (destination.credentials.isNotBlank()) {
        requestBuilder.header("Authorization", "Bearer ${destination.credentials.trim()}")
      }

      val request = requestBuilder.head().build()

      client.newCall(request).execute().use { response ->
        if (response.isSuccessful || response.code in 200..399) {
          ApiResult.Success("Endpoint reachable. HTTP ${response.code}")
        } else {
          ApiResult.Error("Destination returned HTTP ${response.code}: ${response.message}", response.code)
        }
      }
    } catch (e: Exception) {
      ApiResult.Error(e.localizedMessage ?: "Failed to connect to publishing destination.")
    }
  }

  suspend fun publishContent(
    destination: PublishingDestinationEntity,
    title: String,
    body: String,
    status: String = "draft" // "publish" or "draft"
  ): ApiResult<String> = withContext(Dispatchers.IO) {
    if (destination.endpoint.isBlank()) {
      return@withContext ApiResult.Error("Publishing destination endpoint is missing.")
    }

    try {
      val jsonBody = JSONObject().apply {
        put("title", title)
        put("content", body)
        put("status", status)
      }

      val requestBuilder = Request.Builder()
        .url(destination.endpoint.trim())
        .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))

      if (destination.credentials.isNotBlank()) {
        val creds = destination.credentials.trim()
        if (creds.startsWith("Basic ") || creds.startsWith("Bearer ")) {
          requestBuilder.header("Authorization", creds)
        } else {
          requestBuilder.header("Authorization", "Bearer $creds")
        }
      }

      val request = requestBuilder.build()

      client.newCall(request).execute().use { response ->
        val respBody = response.body?.string() ?: ""
        if (response.isSuccessful || response.code in 200..201) {
          ApiResult.Success("Content successfully posted to ${destination.name} (HTTP ${response.code})")
        } else {
          ApiResult.Error("Publishing failed: HTTP ${response.code} - ${response.message}", response.code)
        }
      }
    } catch (e: Exception) {
      ApiResult.Error(e.localizedMessage ?: "Failed to transmit content to publishing endpoint.")
    }
  }
}
