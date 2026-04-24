package my.edu.utar.freshtrackai.ai

import android.content.Context

/**
 * Stores the application context for classes that need global access to it.
 * Only the application context is kept to avoid leaking activity instances.
 */

internal object AppContextProvider {
    @Volatile
    private var appContext: Context? = null

    // Saves the application context during app startup.
    fun initialize(context: Context) {
        appContext = context.applicationContext
    }

    // Returns the stored application context if available.
    fun get(): Context? = appContext
}
