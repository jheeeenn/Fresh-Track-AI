package my.edu.utar.freshtrackai.ai

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import my.edu.utar.freshtrackai.ui.dashboard.ScanCapture


/**
 * Converts a scan capture into a Bitmap for AI processing.
 * Both camera and gallery captures are resolved from their stored Uri.
 */

internal object ScanCaptureBitmapResolver {

    // Loads the image referenced by ScanCapture and decodes it as a Bitmap.
    fun resolve(context: Context, capture: ScanCapture): Bitmap {
        val uri = when (capture) {
            is ScanCapture.Camera -> capture.uri
            is ScanCapture.Gallery -> capture.uri
        }

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source)
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }
    }
}