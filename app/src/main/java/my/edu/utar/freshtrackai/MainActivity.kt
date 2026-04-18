package my.edu.utar.freshtrackai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import my.edu.utar.freshtrackai.ui.dashboard.FreshTrackDashboardScreen
import my.edu.utar.freshtrackai.ui.theme.FreshTrackAITheme

// ai
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import my.edu.utar.freshtrackai.ai.GemmaSmokeTest
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import java.io.File
import my.edu.utar.freshtrackai.ai.GemmaModelStore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val pickGemmaModelLauncher = registerForActivityResult(
            ActivityResultContracts.OpenDocument()
        ) { uri: Uri? ->
            if (uri == null) {
                Log.d("GEMMA_PICKER", "No model selected")
                return@registerForActivityResult
            }

            try {
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )

                val savedPath = copyGemmaModelToInternalStorage(uri)
                GemmaModelStore.saveModelPath(this, savedPath)

                Log.d("GEMMA_PICKER", "Model saved to: $savedPath")
            } catch (e: Exception) {
                Log.e("GEMMA_PICKER", "Failed to save selected model", e)
            }
        }

        if (GemmaModelStore.getModelPath(this).isNullOrBlank()) {
            pickGemmaModelLauncher.launch(arrayOf("*/*"))
        }



        setContent {
            FreshTrackAITheme(darkTheme = false, dynamicColor = false) {
                FreshTrackDashboardScreen(modifier = Modifier.fillMaxSize())
            }
        }
    }

    private fun copyGemmaModelToInternalStorage(uri: Uri): String {
        val modelsDir = File(filesDir, "models")
        if (!modelsDir.exists()) modelsDir.mkdirs()

        val outFile = File(modelsDir, "gemma4.litertlm")

        contentResolver.openInputStream(uri)?.use { input ->
            outFile.outputStream().use { output ->
                input.copyTo(output)
            }
        } ?: error("Failed to open selected model file")

        return outFile.absolutePath
    }
}
