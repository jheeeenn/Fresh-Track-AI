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

/**
 * Manages the local Gemma model lifecycle for this app.
 *
 * This class is responsible for loading the selected LiteRT model file,
 * creating the engine and conversation session, and sending either
 * text-only or image-based prompts to the local model.
 */

internal class GemmaManager(private val context: Context) {

    private var engine: Engine? = null
    private var conversation: Conversation? = null
    private var initialized = false
    private var initializedWithImageSupport = false

    /**
     * Loads and initializes the Gemma model if needed.
     *
     * If the model is already initialized with the required mode
     * (text-only or image-enabled), the existing session is reused.
     */
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

                val modelPath = GemmaModelStore.ensureBundledModelReady(context)
                    .getOrElse { error ->
                        return@withContext Result.failure(
                            IllegalStateException("Bundled Gemma model is not available.", error)
                        )
                    }

                val modelFile = File(modelPath)
                if (!modelFile.exists()) {
                    return@withContext Result.failure(
                        IllegalStateException("Bundled Gemma model file could not be found.")
                    )
                }

                // Reset the previous session before creating a new one.
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

    /**
     * Sends a plain text prompt to Gemma.
     * prompt from prompt factory
     */
    suspend fun sendPrompt(prompt: String): Result<String> {
        return sendContents(
            Contents.of(Content.Text(prompt))
        )
    }

    /**
     * Sends prepared LiteRT contents to Gemma and collects the streamed response.
     *
     * This is the common low-level method used by both text and image prompts.
     */
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


    /**
     * Sends an image together with a text prompt to Gemma.
     *
     * The bitmap is converted into PNG bytes before being wrapped
     * as image content for the model.
     */
    suspend fun sendImagePrompt(bitmap: Bitmap, prompt: String): Result<String> {
        val imageBytes = bitmap.toPngByteArray()

        val contents = Contents.of(
            Content.ImageBytes(imageBytes),
            Content.Text(prompt)
        )

        return sendContents(contents)
    }

    /**
     * Converts a bitmap into PNG byte data for model input.
     */
    private fun Bitmap.toPngByteArray(): ByteArray {
        val stream = ByteArrayOutputStream()
        compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }

    /**
     * Closes the current Gemma session and releases related resources.
     */
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