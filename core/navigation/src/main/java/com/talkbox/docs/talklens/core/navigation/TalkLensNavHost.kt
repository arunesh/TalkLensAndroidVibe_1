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
    cameraScreen: @Composable (onNavigateToTranslation: (String) -> Unit) -> Unit = {},
    galleryScreen: @Composable () -> Unit = {},
    settingsScreen: @Composable () -> Unit = {},
    translationScreen: @Composable (String, () -> Unit) -> Unit = { _, _ -> }
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
            cameraScreen { sourceText ->
                navController.navigate(TalkLensDestination.Translation.createRoute(sourceText))
            }
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
    }
}

// Extension function for easier composable navigation
private fun androidx.navigation.NavGraphBuilder.composable(
    route: String,
    content: @Composable () -> Unit
) {
    androidx.navigation.compose.composable(route = route) { content() }
}
