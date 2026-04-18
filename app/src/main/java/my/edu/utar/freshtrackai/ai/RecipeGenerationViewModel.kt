package my.edu.utar.freshtrackai.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import my.edu.utar.freshtrackai.ui.dashboard.InventoryItem
import my.edu.utar.freshtrackai.ui.dashboard.RecipePreferencesUi

internal class RecipeGenerationViewModel(
    private val useCase: GenerateRecipeUiUseCase = GenerateRecipeUiUseCase()
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecipeGenerationUiState())
    val uiState: StateFlow<RecipeGenerationUiState> = _uiState.asStateFlow()

    fun generateRecipes(
        inventory: List<InventoryItem>,
        preferences: RecipePreferencesUi
    ) {
        if (inventory.isEmpty()) {
            _uiState.value = RecipeGenerationUiState(
                isLoading = false,
                recipes = emptyList(),
                errorMessage = "No inventory items available."
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = RecipeGenerationUiState(isLoading = true)

            try {
                val recipes = useCase.generateFromInventory(
                    inventory = inventory,
                    preferences = preferences
                )

                _uiState.value = RecipeGenerationUiState(
                    isLoading = false,
                    recipes = recipes,
                    errorMessage = null
                )
            } catch (e: Exception) {
                _uiState.value = RecipeGenerationUiState(
                    isLoading = false,
                    recipes = emptyList(),
                    errorMessage = e.message ?: "Failed to generate recipes."
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}