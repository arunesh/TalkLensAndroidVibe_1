package com.talkbox.docs.talklens.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Bottom navigation bar for the main app tabs
 */
@Composable
fun TalkLensBottomBar(
    currentRoute: String?,
    onTabSelected: (MainTab) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(modifier = modifier) {
        bottomNavItems.forEach { item ->
            val selected = currentRoute == item.tab.route

            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.tab.title
                    )
                },
                label = { Text(item.tab.title) },
                selected = selected,
                onClick = { onTabSelected(item.tab) }
            )
        }
    }
}

private data class BottomNavItem(
    val tab: MainTab,
    val icon: ImageVector
)

private val bottomNavItems = listOf(
    BottomNavItem(MainTab.CAMERA, Icons.Default.CameraAlt),
    BottomNavItem(MainTab.GALLERY, Icons.Default.Image),
    BottomNavItem(MainTab.SETTINGS, Icons.Default.Settings)
)
