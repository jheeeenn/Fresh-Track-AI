package my.edu.utar.freshtrackai.ai

import android.content.Context
import java.io.File

internal enum class GemmaModelStatus {
    Ready,
    Missing
}

/**
 * Manages the bundled Gemma model file used by the app.
 * The model is copied from assets into internal storage on first use.
 */
internal object GemmaModelStore {

    private const val PREFS = "gemma_model_store"
    private const val KEY_MODEL_PATH = "model_path"
    private const val BUNDLED_MODEL_FILE_NAME = "gemma4.litertlm"

    private fun saveModelPath(context: Context, absolutePath: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_MODEL_PATH, absolutePath)
            .apply()
    }

    fun getModelPath(context: Context): String? {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_MODEL_PATH, null)
    }

    fun getModelStatus(context: Context): GemmaModelStatus {
        val path = getModelPath(context)
        return if (path != null && File(path).exists()) {
            GemmaModelStatus.Ready
        } else {
            GemmaModelStatus.Missing
        }
    }

    fun ensureBundledModelReady(context: Context): Result<String> {
        return runCatching {
            val modelsDir = File(context.filesDir, "models")
            if (!modelsDir.exists()) {
                modelsDir.mkdirs()
            }

            val outFile = File(modelsDir, BUNDLED_MODEL_FILE_NAME)

            if (!outFile.exists()) {
                context.assets.open(BUNDLED_MODEL_FILE_NAME).use { input ->
                    outFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            }

            saveModelPath(context, outFile.absolutePath)
            outFile.absolutePath
        }
    }
}