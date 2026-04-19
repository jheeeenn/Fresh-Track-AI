package my.edu.utar.freshtrackai.logic

import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import my.edu.utar.freshtrackai.BuildConfig

object AiCategorizer {

    private val API_KEY = BuildConfig.API_KEY

    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = API_KEY
    )

    // ─────────────────────────────────────────────────────────────
    // FUNCTION 1: Asks AI for EXACT DAYS (For your custom items)
    // ─────────────────────────────────────────────────────────────
    suspend fun getEstimatedDays(itemName: String): Int = withContext(Dispatchers.IO) {
        try {
            val prompt = """
                You are a food expiry estimation AI. 
                Estimate the average shelf life in days for an unopened package of "$itemName".
                Reply with ONLY a single integer number representing the days. 
                No text, no punctuation, no explanation.
            """.trimIndent()

            val response = generativeModel.generateContent(prompt)

            // Extract only the numbers from the AI response safely
            val aiAnswer = response.text?.replace(Regex("[^0-9]"), "") ?: ""

            if (aiAnswer.isNotEmpty()) {
                return@withContext aiAnswer.toInt()
            } else {
                return@withContext -1 // AI didn't give a valid number
            }

        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext -1 // Fallback if no internet
        }
    }

    // ─────────────────────────────────────────────────────────────
    // FUNCTION 2: RESTORED! Asks AI for the CATEGORY
    // (This fixes the red line in ShelfLifeRules.kt)
    // ─────────────────────────────────────────────────────────────
    suspend fun categorize(itemName: String): ShelfLifeRules.FoodCategory = withContext(Dispatchers.IO) {
        try {
            val prompt = """
                You are an inventory categorization AI. 
                Look at the following food item: "$itemName".
                Match it to EXACTLY ONE of the following categories:
                DAIRY, EGGS, MEAT_POULTRY, SEAFOOD, FRUITS, VEGETABLES, BAKERY, GRAINS_PASTA, CANNED_GOODS, FROZEN, BEVERAGES, CONDIMENTS, SNACKS, LEFTOVERS, OTHER.
                Reply with ONLY the exact category name and absolutely nothing else. No punctuation, no markdown, no asterisks.
            """.trimIndent()

            val response = generativeModel.generateContent(prompt)

            var aiAnswer = response.text?.uppercase() ?: "OTHER"

            aiAnswer = aiAnswer.replace("*", "")
                .replace("`", "")
                .replace(".", "")
                .replace("CATEGORY:", "")
                .trim()

            return@withContext try {
                ShelfLifeRules.FoodCategory.valueOf(aiAnswer)
            } catch (e: IllegalArgumentException) {
                ShelfLifeRules.FoodCategory.OTHER
            }

        } catch (e: Exception) {
            e.printStackTrace()
            ShelfLifeRules.FoodCategory.OTHER
        }
    }
}