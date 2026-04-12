# FreshTrack AI — UI Handoff (Member 1)

## Current Status
- Current app is **frontend + mock state only**.
- Implemented as **Jetpack Compose (Kotlin)** UI flow.
- No backend/API/database/notification production wiring in this branch yet.

## Pending Mandatory Requirements (for other members)
- Local database + inventory persistence (Room/DAO/CRUD).
- External AI endpoint integration:
  - Smart food detection / receipt parsing.
  - AI recipe suggestion.
  - Shopping list generation logic from backend outputs.
- Expiry estimation business logic (real rules, not mock heuristics).
- Expiry notification system (WorkManager + notification channel + reminders).

## UI Flow Ownership Files
- Navigation/root entrypoint:
  - `app/src/main/java/my/edu/utar/freshtrackai/ui/dashboard/DashboardRoute.kt`
- Screen routing/tab enums:
  - `app/src/main/java/my/edu/utar/freshtrackai/ui/dashboard/DashboardNavigation.kt`
- Screen implementations:
  - Launcher: `LauncherScreen.kt`
  - Home + Expiring: `HomeDashboardScreen.kt`
  - Scan: `ScanScreen.kt`
  - Item Review + Add Missing: `ItemReviewScreens.kt`
  - Recipes: `RecipeScreens.kt`
  - Shopping List: `ShoppingListScreen.kt`
- Shared UI components/tokens:
  - `DashboardAppBars.kt`
  - `DashboardSharedComponents.kt`
  - `DashboardTokens.kt`

## Mock Logic to Replace (Important)
- `DashboardData.kt`
  - `seed*` functions
  - `estimateExpiresInDays`
  - `generateRecipesForPreferences`
  - `addMissingItemsToShoppingList`
- `DashboardRoute.kt`
  - in-memory state using `mutableStateListOf(...)` and local `remember/rememberSaveable` flow state.

## Keep These Contracts Stable (Do Not Break UI Integration)
- Data models in:
  - `app/src/main/java/my/edu/utar/freshtrackai/ui/dashboard/DashboardModels.kt`
- Navigation entrypoint/signature:
  - `fun FreshTrackDashboardScreen(modifier: Modifier = Modifier)` in `DashboardRoute.kt`

## If Sharing as ZIP (instead of Git)
- Exclude generated/heavy folders:
  - `.gradle/`
  - `build/`
  - `app/build/`
- Keep:
  - `app/src/`
  - `gradle/` (wrapper files)
  - root Gradle files (`settings.gradle.kts`, `build.gradle.kts`, `gradle.properties`, `gradlew*`)

## Note to Team
- UI layer is **Compose Kotlin**, **not XML/RecyclerView**.
- Please wire backend/data modules behind existing Compose state contracts first, then refactor only if necessary.
