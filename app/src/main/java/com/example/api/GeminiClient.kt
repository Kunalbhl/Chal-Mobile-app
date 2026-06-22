package com.example.api

import android.util.Log
import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

// --- Data Models matching Gemini API Schema ---

data class GeminiPart(val text: String)
data class GeminiContent(val parts: List<GeminiPart>, val role: String? = null)
data class GeminiSystemInstruction(val parts: List<GeminiPart>)
data class GeminiGenerateRequest(
    val contents: List<GeminiContent>,
    val systemInstruction: GeminiSystemInstruction? = null
)

// Response parsing classes
data class GeminiCandidate(val content: GeminiContent?)
data class GeminiResponse(val candidates: List<GeminiCandidate>?)

object GeminiClient {
    private const val TAG = "GeminiClient"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"
    
    // Default model
    private const val MODEL_NAME = "gemini-3.5-flash"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun generateContent(
        prompt: String,
        systemInstructionText: String? = null,
        history: List<GeminiContent> = emptyList()
    ): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.e(TAG, "Gemini API Key is empty or placeholder! Please set it in AI Studio Secrets.")
            return "Chalo Smart AI Assistant: API key not configured. Please use the Secrets panel in AI Studio to set your GEMINI_API_KEY."
        }

        val requestUrl = "$BASE_URL/$MODEL_NAME:generateContent?key=$apiKey"

        val contentsList = mutableListOf<GeminiContent>()
        // Add chat history if present
        contentsList.addAll(history)
        // Add current prompt
        contentsList.add(GeminiContent(parts = listOf(GeminiPart(text = prompt)), role = "user"))

        val sysInstruction = systemInstructionText?.let {
            GeminiSystemInstruction(parts = listOf(GeminiPart(text = it)))
        }

        val geminiRequest = GeminiGenerateRequest(
            contents = contentsList,
            systemInstruction = sysInstruction
        )

        val jsonAdapter = moshi.adapter(GeminiGenerateRequest::class.java)
        val requestJson = jsonAdapter.toJson(geminiRequest)

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = requestJson.toRequestBody(mediaType)

        val request = Request.Builder()
            .url(requestUrl)
            .post(requestBody)
            .build()

        return try {
            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                val errBody = response.body?.string() ?: ""
                Log.e(TAG, "API Call failed with code ${response.code}: $errBody")
                return "AI Recommendation Engine is offline right now, but here is a quick local lookup. (API Error: ${response.code})"
            }

            val responseString = response.body?.string() ?: ""
            val responseAdapter = moshi.adapter(GeminiResponse::class.java)
            val geminiResponse = responseAdapter.fromJson(responseString)

            geminiResponse?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "No valid response. Please check your query or model credentials."
        } catch (e: Exception) {
            Log.e(TAG, "Exception during API call: ${e.message}", e)
            "Could not connect to the Super App AI Core. Please check your internet connection."
        }
    }
}
