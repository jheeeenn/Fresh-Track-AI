package my.edu.utar.freshtrackai.ai

import android.content.Context

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

    fun clearModelPath(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_MODEL_PATH)
            .apply()
    }
}