package my.edu.utar.freshtrackai.ai

import android.content.Context
import android.content.Intent
import android.net.Uri
import java.io.File

/**
 * Stores the selected local Gemma model path in SharedPreferences.
 * This allows the app to reuse the same model file across launches.
 */
internal object GemmaModelStore {
    private const val PREFS = "gemma_model_store"
    private const val KEY_MODEL_PATH = "model_path"

    fun saveModelPath(context: Context, absolutePath: String) {
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
        val modelPath = getModelPath(context)
        return when {
            modelPath.isNullOrBlank() -> GemmaModelStatus.NotSet
            File(modelPath).exists() -> GemmaModelStatus.Configured
            else -> GemmaModelStatus.MissingFile
        }
    }

    fun importModelFromUri(context: Context, uri: Uri): Result<String> {
        return runCatching {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )

            val modelsDir = File(context.filesDir, "models")
            if (!modelsDir.exists()) {
                modelsDir.mkdirs()
            }

            val outFile = File(modelsDir, "gemma4.litertlm")

            context.contentResolver.openInputStream(uri)?.use { input ->
                outFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            } ?: error("Failed to open selected model file")

            saveModelPath(context, outFile.absolutePath)
            outFile.absolutePath
        }
    }
}

internal enum class GemmaModelStatus {
    Configured,
    MissingFile,
    NotSet
}
