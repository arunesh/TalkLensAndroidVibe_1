package com.talkbox.docs.talklens

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.talkbox.docs.talklens.core.designsystem.theme.TalkLensTheme
import com.talkbox.docs.talklens.core.navigation.MainTab
import com.talkbox.docs.talklens.core.navigation.TalkLensBottomBar
import com.talkbox.docs.talklens.core.navigation.TalkLensDestination
import com.talkbox.docs.talklens.core.navigation.TalkLensNavHost
import com.talkbox.docs.talklens.feature.camera.CameraScreen
import com.talkbox.docs.talklens.feature.gallery.GalleryScreen
import com.talkbox.docs.talklens.feature.settings.SettingsScreen
import com.talkbox.docs.talklens.feature.setup.SetupScreen
import com.talkbox.docs.talklens.feature.translation.MultiPageTranslationScreen
import com.talkbox.docs.talklens.feature.translation.TranslationScreen
import kotlinx.coroutines.launch

/**
 * Main app composable
 */
@Composable
fun TalkLensApp(
    viewModel: TalkLensAppViewModel = hiltViewModel()
) {
    val isSetupCompleted by viewModel.isSetupCompleted.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val coroutineScope = rememberCoroutineScope()

    // Determine start destination based on setup status
    val startDestination = if (isSetupCompleted) {
        TalkLensDestination.Camera.route
    } else {
        TalkLensDestination.Setup.route
    }

    TalkLensTheme {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        // Show bottom bar only for main tabs
        val showBottomBar = currentRoute in listOf(
            TalkLensDestination.Camera.route,
            TalkLensDestination.Gallery.route,
            TalkLensDestination.Settings.route
        )

        Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    TalkLensBottomBar(
                        currentRoute = currentRoute,
                        onTabSelected = { tab ->
                            navController.navigate(tab.route) {
                                // Pop up to the start destination
                                popUpTo(TalkLensDestination.Camera.route) {
                                    saveState = true
                                }
                                // Avoid multiple copies of the same destination
                                launchSingleTop = true
                                // Restore state when reselecting a previously selected item
                                restoreState = true
                            }
                        }
                    )
                }
            }
        ) { paddingValues ->
            TalkLensNavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = Modifier.padding(paddingValues),
                setupScreen = {
                    SetupScreen(
                        onSetupComplete = {
                            coroutineScope.launch {
                                viewModel.markSetupCompleted()
                                navController.navigate(TalkLensDestination.Camera.route) {
                                    popUpTo(TalkLensDestination.Setup.route) {
                                        inclusive = true
                                    }
                                }
                            }
                        }
                    )
                },
                cameraScreen = { onNavigateToTranslation, onNavigateToMultiPageTranslation ->
                    CameraScreen(
                        onNavigateToTranslation = onNavigateToTranslation,
                        onNavigateToMultiPageTranslation = onNavigateToMultiPageTranslation
                    )
                },
                galleryScreen = { onNavigateToTranslation, onNavigateToMultiPageTranslation ->
                    GalleryScreen(
                        onNavigateToTranslation = onNavigateToTranslation,
                        onNavigateToMultiPageTranslation = onNavigateToMultiPageTranslation
                    )
                },
                settingsScreen = { SettingsScreen() },
                translationScreen = { sourceText, onNavigateBack ->
                    TranslationScreen(
                        sourceText = sourceText,
                        onNavigateBack = onNavigateBack
                    )
                },
                multiPageTranslationScreen = { onNavigateBack ->
                    MultiPageTranslationScreen(
                        onNavigateBack = onNavigateBack
                    )
                }
            )
        }
    }
}
