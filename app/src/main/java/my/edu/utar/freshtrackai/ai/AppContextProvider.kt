package my.edu.utar.freshtrackai.ai

import android.content.Context

internal object AppContextProvider {
    @Volatile
    private var appContext: Context? = null

    fun initialize(context: Context) {
        appContext = context.applicationContext
    }

    fun get(): Context? = appContext
}
