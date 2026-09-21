package com.example.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.concurrent.TimeUnit

object ImageStorageUtil {

  private val httpClient = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .build()

  /**
   * Saves an image (from a remote URL, local file path, or base64 data URI)
   * into the device's MediaStore Pictures collection.
   * Returns the Uri of the saved image, or null on failure.
   */
  suspend fun saveImageToGallery(
    context: Context,
    imageSource: String,
    title: String = "FashionEngine_${System.currentTimeMillis()}"
  ): Result<Uri> = withContext(Dispatchers.IO) {
    try {
      val bitmap: Bitmap = when {
        imageSource.startsWith("data:image") -> {
          val base64Data = imageSource.substringAfter("base64,")
          val decodedBytes = Base64.decode(base64Data, Base64.DEFAULT)
          BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
            ?: throw IllegalStateException("Failed to decode base64 image data")
        }
        imageSource.startsWith("http://") || imageSource.startsWith("https://") -> {
          val request = Request.Builder().url(imageSource).build()
          val response = httpClient.newCall(request).execute()
          if (!response.isSuccessful) {
            throw IllegalStateException("HTTP ${response.code} downloading image")
          }
          val bytes = response.body?.bytes() ?: throw IllegalStateException("Empty image response body")
          BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            ?: throw IllegalStateException("Failed to decode downloaded image bytes")
        }
        else -> {
          // Local file path
          val file = File(imageSource)
          if (file.exists()) {
            BitmapFactory.decodeFile(file.absolutePath)
              ?: throw IllegalStateException("Failed to decode local image file")
          } else {
            throw IllegalArgumentException("Invalid image source path")
          }
        }
      }

      val filename = "${title.replace(Regex("[^a-zA-Z0-9_-]"), "_")}_${System.currentTimeMillis()}.jpg"
      val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
          put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/FashionEngine")
          put(MediaStore.MediaColumns.IS_PENDING, 1)
        }
      }

      val resolver = context.contentResolver
      val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        ?: throw IllegalStateException("Failed to create MediaStore entry")

      resolver.openOutputStream(uri).use { outStream: OutputStream? ->
        if (outStream == null) throw IllegalStateException("Cannot open output stream for MediaStore Uri")
        val success = bitmap.compress(Bitmap.CompressFormat.JPEG, 95, outStream)
        if (!success) throw IllegalStateException("Failed to compress bitmap into MediaStore stream")
      }

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        contentValues.clear()
        contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
        resolver.update(uri, contentValues, null, null)
      }

      Result.success(uri)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  /**
   * Saves a byte array (e.g. from Gemini image generation) directly to internal cache
   * and returns the file path, or saves to gallery.
   */
  suspend fun saveBytesToLocalCache(
    context: Context,
    bytes: ByteArray,
    prefix: String = "fashion_img"
  ): String = withContext(Dispatchers.IO) {
    val dir = File(context.filesDir, "fashion_images")
    if (!dir.exists()) dir.mkdirs()
    val file = File(dir, "${prefix}_${System.currentTimeMillis()}.jpg")
    FileOutputStream(file).use { it.write(bytes) }
    file.absolutePath
  }
}
