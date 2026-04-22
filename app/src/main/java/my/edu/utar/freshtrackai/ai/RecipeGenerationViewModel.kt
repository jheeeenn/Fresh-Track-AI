package my.edu.utar.freshtrackai.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import my.edu.utar.freshtrackai.ui.dashboard.InventoryItem
import my.edu.utar.freshtrackai.ui.dashboard.RecipePreferencesUi
import my.edu.utar.freshtrackai.ui.dashboard.RecipeUi

internal class RecipeGenerationViewModel(
    private val useCase: GenerateRecipeUiUseCase = GenerateRecipeUiUseCase()
) : ViewModel() {

    companion object {
        private var cachedRecipes: List<RecipeUi> = emptyList()
    }

    private val _uiState = MutableStateFlow(
        RecipeGenerationUiState(recipes = cachedRecipes)
    )
    val uiState: StateFlow<RecipeGenerationUiState> = _uiState.asStateFlow()

    fun generateRecipes(
        inventory: List<InventoryItem>,
        preferences: RecipePreferencesUi
    ) {
        if (inventory.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = "No inventory items available.",
                processingMessage = null
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                processingMessage = "Preparing inventory summary..."
            )

            try {
                val recipes = useCase.generateFromInventory(
                    inventory = inventory,
                    preferences = preferences,
                    onStatus = { status ->
                        _uiState.value = _uiState.value.copy(processingMessage = status)
                    }
                )

                cachedRecipes = recipes
                _uiState.value = RecipeGenerationUiState(
                    isLoading = false,
                    recipes = recipes,
                    errorMessage = null,
                    processingMessage = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to generate recipes.",
                    processingMessage = null
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
