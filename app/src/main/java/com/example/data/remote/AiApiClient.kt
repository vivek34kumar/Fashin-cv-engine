package com.example.data.remote

import com.example.data.local.entity.AIProviderEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

sealed class ApiResult<out T> {
  data class Success<out T>(val data: T) : ApiResult<T>()
  data class Error(val message: String, val code: Int? = null) : ApiResult<Nothing>()
}

class AiApiClient {
  private val client = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(60, TimeUnit.SECONDS)
    .writeTimeout(30, TimeUnit.SECONDS)
    .build()

  companion object {
    fun maskApiKey(key: String): String {
      if (key.isBlank()) return "Not configured"
      if (key.length <= 8) return "••••••••"
      val prefix = key.take(4)
      val suffix = key.takeLast(4)
      return "$prefix••••••••$suffix"
    }
  }

  suspend fun testConnection(provider: AIProviderEntity): ApiResult<String> = withContext(Dispatchers.IO) {
    if (provider.apiKey.isBlank()) {
      return@withContext ApiResult.Error("API key is missing. Please enter a valid key.")
    }

    try {
      when (provider.providerType.uppercase()) {
        "GEMINI" -> {
          val url = "https://generativelanguage.googleapis.com/v1beta/models?key=${provider.apiKey.trim()}"
          val request = Request.Builder()
            .url(url)
            .get()
            .build()

          client.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
              ApiResult.Success("Connected successfully to Gemini API. HTTP ${response.code}")
            } else {
              val errBody = response.body?.string() ?: ""
              val msg = parseErrorMessage(errBody) ?: "HTTP ${response.code}: ${response.message}"
              ApiResult.Error("Gemini connection failed: $msg", response.code)
            }
          }
        }
        else -> {
          // OpenAI, DeepSeek, NVIDIA, Custom OpenAI-compatible
          val baseEndpoint = if (provider.apiEndpoint.isNotBlank()) {
            provider.apiEndpoint.trim().removeSuffix("/")
          } else {
            "https://api.openai.com/v1"
          }
          val url = "$baseEndpoint/models"
          val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer ${provider.apiKey.trim()}")
            .get()
            .build()

          client.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
              ApiResult.Success("Connected successfully to ${provider.name}. HTTP ${response.code}")
            } else {
              val errBody = response.body?.string() ?: ""
              val msg = parseErrorMessage(errBody) ?: "HTTP ${response.code}: ${response.message}"
              ApiResult.Error("Connection failed: $msg", response.code)
            }
          }
        }
      }
    } catch (e: Exception) {
      ApiResult.Error(e.localizedMessage ?: "Network connection error")
    }
  }

  suspend fun generateContent(
    provider: AIProviderEntity,
    prompt: String,
    systemInstruction: String? = null
  ): ApiResult<String> = withContext(Dispatchers.IO) {
    if (provider.apiKey.isBlank()) {
      return@withContext ApiResult.Error("AI Provider '${provider.name}' is not configured with an API key.")
    }

    try {
      when (provider.providerType.uppercase()) {
        "GEMINI" -> {
          val model = if (provider.model.isNotBlank()) provider.model.trim() else "gemini-3.5-flash"
          val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=${provider.apiKey.trim()}"

          val jsonBody = JSONObject().apply {
            val contentsArray = JSONArray().apply {
              put(JSONObject().apply {
                put("parts", JSONArray().apply {
                  put(JSONObject().apply {
                    put("text", prompt)
                  })
                })
              })
            }
            put("contents", contentsArray)

            if (!systemInstruction.isNullOrBlank()) {
              put("systemInstruction", JSONObject().apply {
                put("parts", JSONArray().apply {
                  put(JSONObject().apply {
                    put("text", systemInstruction)
                  })
                })
              })
            }
          }

          val request = Request.Builder()
            .url(url)
            .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
            .build()

          client.newCall(request).execute().use { response ->
            val bodyString = response.body?.string() ?: ""
            if (response.isSuccessful) {
              val json = JSONObject(bodyString)
              val candidates = json.optJSONArray("candidates")
              val text = candidates?.optJSONObject(0)
                ?.optJSONObject("content")
                ?.optJSONArray("parts")
                ?.optJSONObject(0)
                ?.optString("text")

              if (!text.isNullOrBlank()) {
                ApiResult.Success(text)
              } else {
                ApiResult.Error("Empty response returned by Gemini model.")
              }
            } else {
              val msg = parseErrorMessage(bodyString) ?: "HTTP ${response.code}: ${response.message}"
              ApiResult.Error(msg, response.code)
            }
          }
        }
        else -> {
          // OpenAI / DeepSeek / NVIDIA / Compatible
          val baseEndpoint = if (provider.apiEndpoint.isNotBlank()) {
            provider.apiEndpoint.trim().removeSuffix("/")
          } else {
            "https://api.openai.com/v1"
          }
          val url = "$baseEndpoint/chat/completions"

          val jsonBody = JSONObject().apply {
            put("model", if (provider.model.isNotBlank()) provider.model.trim() else "gpt-4o")
            val messages = JSONArray().apply {
              if (!systemInstruction.isNullOrBlank()) {
                put(JSONObject().apply {
                  put("role", "system")
                  put("content", systemInstruction)
                })
              }
              put(JSONObject().apply {
                put("role", "user")
                put("content", prompt)
              })
            }
            put("messages", messages)
            put("temperature", 0.7)
          }

          val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer ${provider.apiKey.trim()}")
            .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
            .build()

          client.newCall(request).execute().use { response ->
            val bodyString = response.body?.string() ?: ""
            if (response.isSuccessful) {
              val json = JSONObject(bodyString)
              val choices = json.optJSONArray("choices")
              val content = choices?.optJSONObject(0)
                ?.optJSONObject("message")
                ?.optString("content")

              if (!content.isNullOrBlank()) {
                ApiResult.Success(content)
              } else {
                ApiResult.Error("Empty response returned by provider.")
              }
            } else {
              val msg = parseErrorMessage(bodyString) ?: "HTTP ${response.code}: ${response.message}"
              ApiResult.Error(msg, response.code)
            }
          }
        }
      }
    } catch (e: Exception) {
      ApiResult.Error(e.localizedMessage ?: "Generation failed due to network error.")
    }
  }

  suspend fun generateImage(
    provider: AIProviderEntity?,
    prompt: String,
    size: String = "1024x1024",
    count: Int = 1
  ): ApiResult<List<String>> = withContext(Dispatchers.IO) {
    if (provider == null || provider.apiKey.isBlank()) {
      return@withContext ApiResult.Error("Image Provider Not Configured. Please configure an active AI/Image provider.")
    }

    try {
      if (provider.providerType.equals("GEMINI", ignoreCase = true)) {
        // Gemini image generation using gemini-2.5-flash-image
        val model = "gemini-2.5-flash-image"
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=${provider.apiKey.trim()}"

        val jsonBody = JSONObject().apply {
          val contentsArray = JSONArray().apply {
            put(JSONObject().apply {
              put("parts", JSONArray().apply {
                put(JSONObject().apply {
                  put("text", prompt)
                })
              })
            })
          }
          put("contents", contentsArray)
          put("generationConfig", JSONObject().apply {
            put("responseModalities", JSONArray().apply {
              put("IMAGE")
              put("TEXT")
            })
            put("imageConfig", JSONObject().apply {
              put("aspectRatio", "1:1")
              put("imageSize", "1K")
            })
          })
        }

        val request = Request.Builder()
          .url(url)
          .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
          .build()

        client.newCall(request).execute().use { response ->
          val bodyString = response.body?.string() ?: ""
          if (response.isSuccessful) {
            val json = JSONObject(bodyString)
            val candidates = json.optJSONArray("candidates")
            val urls = mutableListOf<String>()
            if (candidates != null) {
              for (i in 0 until candidates.length()) {
                val cand = candidates.optJSONObject(i)
                val content = cand?.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                if (parts != null) {
                  for (p in 0 until parts.length()) {
                    val part = parts.optJSONObject(p)
                    val inlineData = part?.optJSONObject("inlineData")
                    if (inlineData != null) {
                      val mimeType = inlineData.optString("mimeType", "image/png")
                      val base64Data = inlineData.optString("data")
                      if (base64Data.isNotBlank()) {
                        urls.add("data:$mimeType;base64,$base64Data")
                      }
                    }
                  }
                }
              }
            }
            if (urls.isNotEmpty()) {
              ApiResult.Success(urls)
            } else {
              ApiResult.Error("Gemini completed without image data.")
            }
          } else {
            val msg = parseErrorMessage(bodyString) ?: "HTTP ${response.code}: ${response.message}"
            ApiResult.Error(msg, response.code)
          }
        }
      } else {
        // Direct call to OpenAI / compatible images/generations
        val baseEndpoint = if (provider.apiEndpoint.isNotBlank()) {
          provider.apiEndpoint.trim().removeSuffix("/")
        } else {
          "https://api.openai.com/v1"
        }
        val url = "$baseEndpoint/images/generations"

        val jsonBody = JSONObject().apply {
          put("prompt", prompt)
          put("n", count)
          put("size", size)
        }

        val request = Request.Builder()
          .url(url)
          .header("Authorization", "Bearer ${provider.apiKey.trim()}")
          .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
          .build()

        client.newCall(request).execute().use { response ->
          val bodyString = response.body?.string() ?: ""
          if (response.isSuccessful) {
            val json = JSONObject(bodyString)
            val dataArray = json.optJSONArray("data")
            val urls = mutableListOf<String>()
            if (dataArray != null) {
              for (i in 0 until dataArray.length()) {
                val item = dataArray.optJSONObject(i)
                val urlStr = item?.optString("url")
                if (!urlStr.isNullOrBlank()) urls.add(urlStr)
              }
            }
            if (urls.isNotEmpty()) {
              ApiResult.Success(urls)
            } else {
              ApiResult.Error("No image URL returned by provider.")
            }
          } else {
            val msg = parseErrorMessage(bodyString) ?: "HTTP ${response.code}: ${response.message}"
            ApiResult.Error(msg, response.code)
          }
        }
      }
    } catch (e: Exception) {
      ApiResult.Error(e.localizedMessage ?: "Image generation request failed.")
    }
  }

  private fun parseErrorMessage(rawJson: String): String? {
    return try {
      val json = JSONObject(rawJson)
      if (json.has("error")) {
        val errObj = json.optJSONObject("error")
        if (errObj != null) {
          errObj.optString("message")
        } else {
          json.optString("error")
        }
      } else {
        null
      }
    } catch (e: Exception) {
      null
    }
  }
}
