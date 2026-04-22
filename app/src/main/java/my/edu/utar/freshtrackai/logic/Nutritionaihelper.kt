package my.edu.utar.freshtrackai.logic

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import my.edu.utar.freshtrackai.BuildConfig

/**
 * NutritionAiHelper.kt
 * Logic Layer — Nutrition Info AI Feature
 *
 * Two entry points:
 *  1. [getNutritionFromLabel]  — takes a camera/gallery Bitmap of a food label,
 *     sends it to Gemini Vision, and returns parsed nutrition text.
 *  2. [estimateNutritionByName] — takes a food item name (no image), asks
 *     Gemini to estimate typical nutrition info as plain text.
 *
 * Both functions are suspend functions; call them from a coroutineScope or
 * viewModelScope. Both return a [NutritionResult] which is either Success
 * or Failure with a reason string.
 *
 * Usage in AddMissingItemScreen (ItemReviewScreens.kt):
 *
 *   // 1. Quick Scan Label (user picks/takes a photo of the label)
 *   val result = NutritionAiHelper.getNutritionFromLabel(context, labelBitmap)
 *
 *   // 2. Auto-fill when user taps "Generate via AI" or leaves field blank
 *   val result = NutritionAiHelper.estimateNutritionByName(itemName, categoryName)
 *
 *   when (result) {
 *       is NutritionResult.Success -> onDraftChange(draft.copy(nutritionNotes = result.text))
 *       is NutritionResult.Failure -> showToast(result.reason)
 *   }
 */
object NutritionAiHelper {

    private const val TAG = "NutritionAiHelper"

    // ─── Result type ──────────────────────────────────────────────────────────

    sealed class NutritionResult {
        /** Nutrition text ready to be placed in the nutritionNotes field. */
        data class Success(val text: String) : NutritionResult()

        /** Something went wrong – show this to the user as a toast/snackbar. */
        data class Failure(val reason: String) : NutritionResult()
    }

    // ─── Shared model (lazy – created once) ───────────────────────────────────

    private val geminiModel by lazy {
        GenerativeModel(
            modelName = "gemini-2.5-flash",
            apiKey = BuildConfig.API_KEY
        )
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ENTRY POINT 1: Quick Scan Label  (image → nutrition text)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Reads a Bitmap of a food label/packaging and extracts the nutrition panel.
     *
     * @param bitmap  The label image captured by camera or chosen from gallery.
     * @return        [NutritionResult.Success] with a compact summary string, or
     *                [NutritionResult.Failure] with an error message.
     */
    suspend fun getNutritionFromLabel(bitmap: Bitmap): NutritionResult =
        withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Sending label image to Gemini Vision…")

                val prompt = """
                    You are a nutrition label reader AI.
                    Look at this food packaging / nutrition label image carefully.
                    
                    Extract ONLY the key nutritional values that are clearly printed.
                    Format the result as a SHORT, human-readable single line like this example:
                    "120 kcal / serving  
                    Protein: 5g 
                    Fat: 4g 
                    Carbs: 18g 
                    Sugar: 6g 
                    Sodium: 210mg"
                    
                    Rules:
                    - Include only values that are clearly visible and legible on the label.
                    - If serving size is shown, include it (e.g., "per 100g" or "per serving").
                    - Do NOT include price, barcode, brand name, or marketing text.
                    - If the label is unreadable or not a food label, reply exactly: UNREADABLE
                    - Return ONLY the formatted string or UNREADABLE. No extra text.
                """.trimIndent()

                val response = geminiModel.generateContent(
                    content {
                        image(bitmap)
                        text(prompt)
                    }
                )

                val raw = response.text?.trim().orEmpty()
                Log.d(TAG, "Gemini label response: $raw")

                if (raw.isBlank() || raw.uppercase() == "UNREADABLE") {
                    NutritionResult.Failure("Label was unreadable. Please try a clearer photo.")
                } else {
                    NutritionResult.Success(raw)
                }

            } catch (e: Exception) {
                Log.e(TAG, "getNutritionFromLabel failed", e)
                NutritionResult.Failure("Could not read label: ${e.message ?: "unknown error"}")
            }
        }

    /**
     * Convenience overload that accepts a Uri (e.g. from camera or gallery launcher)
     * and decodes it to a Bitmap before calling [getNutritionFromLabel].
     */
    suspend fun getNutritionFromLabelUri(context: Context, uri: Uri): NutritionResult =
        withContext(Dispatchers.IO) {
            try {
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ImageDecoder.decodeBitmap(
                        ImageDecoder.createSource(context.contentResolver, uri)
                    )
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }
                getNutritionFromLabel(bitmap)
            } catch (e: Exception) {
                Log.e(TAG, "getNutritionFromLabelUri failed", e)
                NutritionResult.Failure("Could not load image: ${e.message ?: "unknown error"}")
            }
        }

    // ─────────────────────────────────────────────────────────────────────────
    // ENTRY POINT 2: Estimate by Name  (no image → estimated nutrition text)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Asks Gemini to provide a typical nutrition estimate for a food item
     * identified only by name (and optionally its category).
     *
     * Call this when:
     *  - The user submits/saves without filling in nutritionNotes, OR
     *  - The user taps an "Auto-fill nutrition" button.
     *
     * @param itemName     The food name, e.g. "Whole Milk", "Brown Rice".
     * @param categoryName Optional category hint, e.g. "Dairy", "Grains & Pasta".
     * @return             [NutritionResult.Success] with a compact nutrition line, or
     *                     [NutritionResult.Failure].
     */
    suspend fun estimateNutritionByName(
        itemName: String,
        categoryName: String = ""
    ): NutritionResult = withContext(Dispatchers.IO) {
        try {
            val categoryHint = if (categoryName.isNotBlank()) " (category: $categoryName)" else ""

            Log.d(TAG, "Estimating nutrition for: $itemName$categoryHint")

            val prompt = """
                You are a nutrition database AI.
                Provide a typical nutritional estimate for: "$itemName"$categoryHint.
                
                Format the result as a SHORT, human-readable single line like this example:
                "61 kcal / 100ml 
                Protein: 3.2g 
                Fat: 3.3g 
                Carbs: 4.8g 
                Sugar: 4.8g 
                Calcium: 120mg"
                
                Rules:
                - Use "per 100g" or "per 100ml" as the base unit (whichever is appropriate).
                - Include the 4-5 most relevant nutrients for this type of food.
                - Use average/typical values for a standard version of this food.
                - If the item is a brand product, use generic equivalent values.
                - Return ONLY the formatted string. No extra text, no markdown.
            """.trimIndent()

            val response = try {
                geminiModel.generateContent(prompt)
            } catch (e: Exception) {
                return@withContext NutritionResult.Failure("Gemini is busy, please try again in a moment.")
            }
            val raw = response.text?.trim().orEmpty()

            Log.d(TAG, "Gemini nutrition estimate: $raw")

            if (raw.isBlank()) {
                NutritionResult.Failure("AI did not return nutrition data. Try entering manually.")
            } else {
                NutritionResult.Success(raw)
            }

        } catch (e: Exception) {
            Log.e(TAG, "estimateNutritionByName failed", e)
            NutritionResult.Failure("Could not estimate nutrition: ${e.message ?: "unknown error"}")
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helper: Should we auto-fill on save?
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns true if the nutritionNotes field is empty or contains only a
     * placeholder/default value — meaning we should auto-estimate on save.
     *
     * Plug this into draftToReviewItem / draftToInventoryItem in DashboardData.kt:
     *
     *   if (NutritionAiHelper.shouldAutoFill(draft.nutritionNotes)) {
     *       val result = NutritionAiHelper.estimateNutritionByName(draft.name, draft.category.label)
     *       if (result is NutritionResult.Success) nutritionText = result.text
     *   }
     */
    fun shouldAutoFill(nutritionNotes: String): Boolean {
        val cleaned = nutritionNotes.trim().lowercase()
        return cleaned.isBlank() ||
                cleaned == "not provided" ||
                cleaned.startsWith("quick scan label (mock)") ||
                cleaned.startsWith("e.g.")
    }
}