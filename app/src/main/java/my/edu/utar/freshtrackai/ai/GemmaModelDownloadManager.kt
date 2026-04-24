package my.edu.utar.freshtrackai.ai

import android.app.DownloadManager
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import kotlin.math.roundToInt

internal data class GemmaModelDownloadSpec(
    val url: String,
    val fileName: String,
    val title: String,
    val description: String
)

internal object GemmaModelDownloadManager {
    private const val MODEL_URL =
        "https://huggingface.co/litert-community/gemma-4-E2B-it-litert-lm/resolve/main/gemma-4-E2B-it.litertlm"
    private const val MODEL_FILE_NAME = "gemma-4-E2B-it.litertlm"

    fun downloadSpec(): GemmaModelDownloadSpec {
        return GemmaModelDownloadSpec(
            url = MODEL_URL,
            fileName = MODEL_FILE_NAME,
            title = "Download Gemma 4 Model",
            description = "Downloads $MODEL_FILE_NAME to your Downloads folder."
        )
    }

    fun enqueueDownload(context: Context): Long {
        val spec = downloadSpec()
        val request = DownloadManager.Request(Uri.parse(spec.url))
            .setTitle(spec.title)
            .setDescription(spec.description)
            .setMimeType("application/octet-stream")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, spec.fileName)

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        return downloadManager.enqueue(request)
    }

    fun queryDownloadStatus(context: Context, downloadId: Long): GemmaModelDownloadStatus {
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val query = DownloadManager.Query().setFilterById(downloadId)
        downloadManager.query(query).use { cursor ->
            if (!cursor.moveToFirst()) {
                return GemmaModelDownloadStatus.NotFound
            }

            val status = cursor.int(DownloadManager.COLUMN_STATUS)
            val downloadedBytes = cursor.long(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
            val totalBytes = cursor.long(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
            val reason = cursor.int(DownloadManager.COLUMN_REASON)

            return when (status) {
                DownloadManager.STATUS_PENDING -> GemmaModelDownloadStatus.Pending
                DownloadManager.STATUS_PAUSED -> GemmaModelDownloadStatus.Paused
                DownloadManager.STATUS_RUNNING -> GemmaModelDownloadStatus.Running(
                    downloadedBytes = downloadedBytes,
                    totalBytes = totalBytes
                )
                DownloadManager.STATUS_SUCCESSFUL -> GemmaModelDownloadStatus.Successful
                DownloadManager.STATUS_FAILED -> GemmaModelDownloadStatus.Failed(reason)
                else -> GemmaModelDownloadStatus.NotFound
            }
        }
    }
}

internal sealed class GemmaModelDownloadStatus {
    data object Pending : GemmaModelDownloadStatus()
    data object Paused : GemmaModelDownloadStatus()
    data class Running(
        val downloadedBytes: Long,
        val totalBytes: Long
    ) : GemmaModelDownloadStatus()
    data object Successful : GemmaModelDownloadStatus()
    data class Failed(val reason: Int) : GemmaModelDownloadStatus()
    data object NotFound : GemmaModelDownloadStatus()
}

internal fun gemmaDownloadStatusMessage(status: GemmaModelDownloadStatus): String {
    return when (status) {
        GemmaModelDownloadStatus.Pending -> "Gemma 4 download queued…"
        GemmaModelDownloadStatus.Paused -> "Gemma 4 download paused."
        is GemmaModelDownloadStatus.Running -> {
            val percent = if (status.totalBytes > 0) {
                ((status.downloadedBytes.toDouble() / status.totalBytes.toDouble()) * 100)
                    .roundToInt()
                    .coerceIn(0, 100)
            } else {
                null
            }
            if (percent != null) {
                "Downloading Gemma 4… $percent%"
            } else {
                "Downloading Gemma 4…"
            }
        }
        GemmaModelDownloadStatus.Successful ->
            "Gemma 4 download complete. Tap Choose Model to import it."
        is GemmaModelDownloadStatus.Failed ->
            "Gemma 4 download failed. Try again."
        GemmaModelDownloadStatus.NotFound ->
            "Gemma 4 download not found."
    }
}

private fun Cursor.int(column: String): Int =
    getInt(getColumnIndexOrThrow(column))

private fun Cursor.long(column: String): Long =
    getLong(getColumnIndexOrThrow(column))
