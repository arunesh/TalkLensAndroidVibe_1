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
    cameraScreen: @Composable (
        onNavigateToTranslation: (String) -> Unit,
        onNavigateToMultiPageTranslation: (String) -> Unit
    ) -> Unit = { _, _ -> },
    galleryScreen: @Composable () -> Unit = {},
    settingsScreen: @Composable () -> Unit = {},
    translationScreen: @Composable (String, () -> Unit) -> Unit = { _, _ -> },
    multiPageTranslationScreen: @Composable (() -> Unit) -> Unit = {}
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
            cameraScreen(
                onNavigateToTranslation = { sourceText ->
                    navController.navigate(TalkLensDestination.Translation.createRoute(sourceText))
                },
                onNavigateToMultiPageTranslation = { documentId ->
                    navController.navigate(TalkLensDestination.MultiPageTranslation.createRoute(documentId))
                }
            )
        }

        composable(TalkLensDestination.Gallery.route) {
            galleryScreen()
        }

        composable(TalkLensDestination.Settings.route) {
            settingsScreen()
        }

        // Translation screen
        composable(TalkLensDestination.Translation.route) { backStackEntry ->
            val encodedText = backStackEntry.arguments?.getString("sourceText") ?: ""
            val sourceText = java.net.URLDecoder.decode(encodedText, "UTF-8")
            translationScreen(sourceText) {
                navController.popBackStack()
            }
        }

        // Multi-page translation screen
        composable(TalkLensDestination.MultiPageTranslation.route) {
            multiPageTranslationScreen {
                navController.popBackStack()
            }
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
