package my.edu.utar.freshtrackai.ai

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import my.edu.utar.freshtrackai.ai.model.RecipeSuggestionResult
import my.edu.utar.freshtrackai.ai.util.PromptFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import com.google.gson.Gson

class GeminiCloudFoodExtractor(
    private val apiKey: String
) : CloudFoodExtractor {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .build()
    private val gson = Gson()

    override suspend fun suggestRecipes(inventorySummary: String): RecipeSuggestionResult {
        return withContext(Dispatchers.IO) {
            val url =
                "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"

            val prompt = PromptFactory.recipePrompt(inventorySummary)

            val payload = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", prompt)
                            })
                        })
                    })
                })

                put("generationConfig", JSONObject().apply {
                    put("responseMimeType", "application/json")
                })
            }

            val request = Request.Builder()
                .url(url)
                .post(payload.toString().toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()

                Log.d("GEMINI_RECIPE_RAW", body)
                Log.d("GEMINI_RECIPE_RAW", "Recipe response received successfully")

                if (!response.isSuccessful) {
                    error("Gemini recipe HTTP ${response.code}: $body")
                }

                val root = JSONObject(body)
                val text = root
                    .getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text")

                gson.fromJson(text, RecipeSuggestionResult::class.java)
            }
        }
    }
}