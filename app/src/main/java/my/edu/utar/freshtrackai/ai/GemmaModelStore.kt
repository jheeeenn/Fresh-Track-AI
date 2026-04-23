package my.edu.utar.freshtrackai.ai

import android.content.Context

/**
 * Stores the selected local Gemma model path in SharedPreferences.
 * This allows the app to reuse the same model file across launches.
 */
internal object GemmaModelStore {
    private const val PREFS = "gemma_model_store"
    private const val KEY_MODEL_PATH = "model_path"

    // Saves the absolute file path of the selected Gemma model.
    fun saveModelPath(context: Context, absolutePath: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_MODEL_PATH, absolutePath)
            .apply()
    }

    // Returns the saved model path, or null if none has been selected.
    fun getModelPath(context: Context): String? {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_MODEL_PATH, null)
    }

}