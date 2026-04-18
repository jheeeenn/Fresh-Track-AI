package my.edu.utar.freshtrackai.ai

import android.content.Context
import android.util.Log

import com.google.ai.edge.litertlm.Conversation
import com.google.ai.edge.litertlm.Contents
import com.google.ai.edge.litertlm.Message
import com.google.ai.edge.litertlm.Content
internal object GemmaSmokeTest {

    suspend fun run(context: Context) {
        try {
            val manager = GemmaManager(context)

            val initResult = manager.ensureInitialized()
            initResult.getOrElse { throw it }

            Log.d("GEMMA_SMOKE", "Model initialized successfully")

            val response = manager.sendPrompt(
                """
                Return only this JSON:
                {
                  "status": "ok",
                  "message": "gemma works"
                }
                """.trimIndent()
            ).getOrElse { throw it }

            Log.d("GEMMA_SMOKE", "Response: $response")
        } catch (e: Exception) {
            Log.e("GEMMA_SMOKE", "Gemma smoke test failed", e)
        }
    }
}