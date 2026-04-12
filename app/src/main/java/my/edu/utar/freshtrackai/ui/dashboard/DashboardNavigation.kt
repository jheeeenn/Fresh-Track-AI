package my.edu.utar.freshtrackai.ui.dashboard

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FormatListBulleted
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.RestaurantMenu
import androidx.compose.ui.graphics.vector.ImageVector

internal enum class WiseScreen {
    AppLauncher,
    MainDashboard,
    ExpiringSoonAll,
    SmartScan,
    AddMissingItem,
    ItemReview,
    RecipeViewAll,
    ShoppingList,
    RecipeDetails,
    AiRecipes
}

internal enum class RootTab(val label: String, val icon: ImageVector) {
    Home("Home", Icons.Outlined.Home),
    Scan("Scan", Icons.Outlined.QrCodeScanner),
    Recipe("Recipe", Icons.Outlined.RestaurantMenu),
    List("List", Icons.Outlined.FormatListBulleted)
}

