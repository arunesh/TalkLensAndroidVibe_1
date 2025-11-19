package com.talkbox.docs.talklens.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost

/**
 * Main navigation host for the TalkLens app
 */
@Composable
fun TalkLensNavHost(
    navController: NavHostController,
    startDestination: String,
    modifier: Modifier = Modifier,
    setupScreen: @Composable () -> Unit = {},
    cameraScreen: @Composable () -> Unit = {},
    galleryScreen: @Composable () -> Unit = {},
    settingsScreen: @Composable () -> Unit = {},
    translationResultScreen: @Composable (String) -> Unit = {}
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // Setup screen (first launch only)
        composable(TalkLensDestination.Setup.route) {
            setupScreen()
        }

        // Main tabs
        composable(TalkLensDestination.Camera.route) {
            cameraScreen()
        }

        composable(TalkLensDestination.Gallery.route) {
            galleryScreen()
        }

        composable(TalkLensDestination.Settings.route) {
            settingsScreen()
        }

        // Translation result screen
        composable(TalkLensDestination.TranslationResult.route) { backStackEntry ->
            val documentId = backStackEntry.arguments?.getString("documentId") ?: ""
            translationResultScreen(documentId)
        }
    }
}

// Extension function for easier composable navigation
private fun androidx.navigation.NavGraphBuilder.composable(
    route: String,
    content: @Composable () -> Unit
) {
    androidx.navigation.compose.composable(route = route) { content() }
}
