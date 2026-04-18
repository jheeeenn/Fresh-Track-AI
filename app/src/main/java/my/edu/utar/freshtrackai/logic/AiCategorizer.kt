package my.edu.utar.freshtrackai.logic

import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * AiCategorizer.kt
 * Member 3 — AI Integration
 * * Uses the Gemini API to intelligently categorize unknown food items.
 */
object AiCategorizer {

    // IMPORTANT: Get a free API key from https://aistudio.google.com/
    // For a production app, never hardcode this, but for a university assignment, it's fine.
    private const val API_KEY = BuildConfig.API_KEY

    // We use the "flash" model because it is designed to be extremely fast
    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = API_KEY
    )

    /**
     * Sends the item name to the AI and forces it to reply with exactly one of our Enum categories.
     */
    suspend fun categorize(itemName: String): ShelfLifeRules.FoodCategory = withContext(Dispatchers.IO) {
        try {
            // Strict prompt engineering to prevent the AI from talking too much
            val prompt = """
                You are an inventory categorization AI. 
                Look at the following food item: "$itemName".
                Match it to EXACTLY ONE of the following categories:
                DAIRY, EGGS, MEAT_POULTRY, SEAFOOD, FRUITS, VEGETABLES, BAKERY, GRAINS_PASTA, CANNED_GOODS, FROZEN, BEVERAGES, CONDIMENTS, SNACKS, LEFTOVERS, OTHER.
                Reply with ONLY the exact category name and absolutely nothing else. No punctuation.
            """.trimIndent()

            // Ask the AI
            val response = generativeModel.generateContent(prompt)

            // Clean the response (e.g., remove invisible spaces or newlines)
            val aiAnswer = response.text?.trim()?.uppercase() ?: "OTHER"

            // Convert the String back into your FoodCategory Enum safely
            return@withContext try {
                ShelfLifeRules.FoodCategory.valueOf(aiAnswer)
            } catch (e: IllegalArgumentException) {
                // If the AI hallucinates a weird word, fallback to OTHER safely
                ShelfLifeRules.FoodCategory.OTHER
            }

        } catch (e: Exception) {
            e.printStackTrace()
            // If there's no internet connection, fallback safely
            ShelfLifeRules.FoodCategory.OTHER
        }
    }
}