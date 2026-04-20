package my.edu.utar.freshtrackai.ai

import android.content.Context
import android.graphics.Bitmap
import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.Content
import com.google.ai.edge.litertlm.Contents
import com.google.ai.edge.litertlm.Conversation
import com.google.ai.edge.litertlm.ConversationConfig
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File

internal class GemmaManager(private val context: Context) {

    private var engine: Engine? = null
    private var conversation: Conversation? = null
    private var initialized = false
    private var initializedWithImageSupport = false

    suspend fun ensureInitialized(enableImage: Boolean = false): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                if (
                    initialized &&
                    engine != null &&
                    conversation != null &&
                    initializedWithImageSupport == enableImage
                ) {
                    return@withContext Result.success(Unit)
                }

                val modelPath = GemmaModelStore.getModelPath(context)
                    ?: return@withContext Result.failure(
                        IllegalStateException("No Gemma model selected yet.")
                    )

                val modelFile = File(modelPath)
                if (!modelFile.exists()) {
                    return@withContext Result.failure(
                        IllegalStateException("Selected Gemma model file no longer exists.")
                    )
                }

                close()

                val engineConfig = EngineConfig(
                    modelPath = modelFile.absolutePath,
                    backend = Backend.CPU(),
                    visionBackend = if (enableImage) Backend.CPU() else null,
                    audioBackend = Backend.CPU(),
                    cacheDir = context.cacheDir.absolutePath
                )

                val newEngine = Engine(engineConfig)
                newEngine.initialize()

                val newConversation = newEngine.createConversation(
                    ConversationConfig()
                )

                engine = newEngine
                conversation = newConversation
                initialized = true
                initializedWithImageSupport = enableImage

                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun sendPrompt(prompt: String): Result<String> {
        return sendContents(
            Contents.of(Content.Text(prompt))
        )
    }

    suspend fun sendContents(contents: Contents): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val conv = conversation
                    ?: return@withContext Result.failure(
                        IllegalStateException("Gemma model is not initialized")
                    )

                val sb = StringBuilder()

                conv.sendMessageAsync(contents).collect { chunk ->
                    sb.append(chunk.toString())
                }

                Result.success(sb.toString())
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun sendImagePrompt(bitmap: Bitmap, prompt: String): Result<String> {
        val imageBytes = bitmap.toPngByteArray()

        val contents = Contents.of(
            Content.ImageBytes(imageBytes),
            Content.Text(prompt)
        )

        return sendContents(contents)
    }


    private fun Bitmap.toPngByteArray(): ByteArray {
        val stream = ByteArrayOutputStream()
        compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }

    fun close() {
        try {
            conversation?.close()
        } catch (_: Exception) {
        }

        try {
            engine?.close()
        } catch (_: Exception) {
        }

        conversation = null
        engine = null
        initialized = false
        initializedWithImageSupport = false
    }
}