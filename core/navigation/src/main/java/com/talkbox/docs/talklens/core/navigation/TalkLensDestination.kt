package com.talkbox.docs.talklens.core.navigation

/**
 * Sealed class representing all navigation destinations in the app
 */
sealed class TalkLensDestination(val route: String) {

    // Setup flow
    data object Setup : TalkLensDestination("setup")

    // Main app tabs
    data object Camera : TalkLensDestination("camera")
    data object Gallery : TalkLensDestination("gallery")
    data object Settings : TalkLensDestination("settings")

    // Translation results (shared across features)
    data object TranslationResult : TalkLensDestination("translation_result/{documentId}") {
        fun createRoute(documentId: String) = "translation_result/$documentId"
    }
}

/**
 * Represents the main bottom navigation tabs
 */
enum class MainTab(val route: String, val title: String) {
    CAMERA(TalkLensDestination.Camera.route, "Camera"),
    GALLERY(TalkLensDestination.Gallery.route, "Gallery"),
    SETTINGS(TalkLensDestination.Settings.route, "Settings");

    companion object {
        fun fromRoute(route: String?): MainTab? = entries.find { it.route == route }
    }
}
