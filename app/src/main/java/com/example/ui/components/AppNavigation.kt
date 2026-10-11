package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag

enum class AppScreen(
    val title: String,
    val icon: ImageVector,
    val testTag: String
) {
    DASHBOARD("Beranda", Icons.Default.Dashboard, "nav_dashboard"),
    SCHEDULES("Jadwal", Icons.Default.Schedule, "nav_schedules"),
    TEACHERS("Data Guru", Icons.Default.People, "nav_teachers"),
    ATTENDANCE("Presensi", Icons.Default.HowToReg, "nav_attendance"),
    LOGS("Log WA", Icons.Default.Chat, "nav_logs"),
    DATABASE_INSTALL("Database SQL", Icons.Default.Storage, "nav_database"),
    SETTINGS("Pengaturan", Icons.Default.Settings, "nav_settings")
}

@Composable
fun AppBottomBar(
    currentScreen: AppScreen,
    onNavigate: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier.testTag("bottom_nav_bar"),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        AppScreen.values().forEach { screen ->
            val isSelected = currentScreen == screen
            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(screen) },
                icon = {
                    Icon(
                        imageVector = screen.icon,
                        contentDescription = screen.title
                    )
                },
                label = { Text(screen.title) },
                modifier = Modifier.testTag(screen.testTag)
            )
        }
    }
}

@Composable
fun AppSideNavRail(
    currentScreen: AppScreen,
    onNavigate: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationRail(
        modifier = modifier.testTag("side_nav_rail"),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        AppScreen.values().forEach { screen ->
            val isSelected = currentScreen == screen
            NavigationRailItem(
                selected = isSelected,
                onClick = { onNavigate(screen) },
                icon = {
                    Icon(
                        imageVector = screen.icon,
                        contentDescription = screen.title
                    )
                },
                label = { Text(screen.title) },
                modifier = Modifier.testTag("rail_${screen.testTag}")
            )
        }
    }
}
