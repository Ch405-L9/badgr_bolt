package com.badgr.orbreader

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.badgr.orbreader.ui.library.LibraryScreen
import com.badgr.orbreader.ui.reader.ReaderScreen
import com.badgr.orbreader.ui.stats.StatsScreen
import com.badgr.orbreader.ui.settings.SettingsScreen
import com.badgr.orbreader.ui.theme.ReaderColors

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    data object Library  : Screen("library",  "Library",  Icons.Default.CollectionsBookmark)
    data object Stats    : Screen("stats",    "Stats",    Icons.Default.QueryStats)
    data object Settings : Screen("settings", "Settings", Icons.Default.Settings)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                val navController = rememberNavController()
                
                // Use explicit state collection to ensure the 'destination' property is visible to the compiler
                val navBackStackEntryState = navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntryState.value?.destination

                // Hide bottom nav when in the Reader Screen for maximum focus
                val showBottomNav = currentDestination?.route?.startsWith("reader") != true

                Scaffold(
                    bottomBar = {
                        if (showBottomNav) {
                            NavigationBar(
                                containerColor = ReaderColors.background,
                                contentColor = ReaderColors.orpFocal
                            ) {
                                val items = listOf(Screen.Library, Screen.Stats, Screen.Settings)
                                items.forEach { screen ->
                                    val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                                    NavigationBarItem(
                                        icon = { Icon(screen.icon, contentDescription = null) },
                                        label = { Text(screen.label) },
                                        selected = isSelected,
                                        onClick = {
                                            navController.navigate(screen.route) {
                                                popUpTo(navController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = ReaderColors.orpFocal,
                                            selectedTextColor = ReaderColors.orpFocal,
                                            indicatorColor = ReaderColors.orpFocal.copy(alpha = 0.1f),
                                            unselectedIconColor = ReaderColors.textDimmed,
                                            unselectedTextColor = ReaderColors.textDimmed
                                        )
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    Surface(
                        modifier = Modifier.padding(innerPadding),
                        color = ReaderColors.background
                    ) {
                        NavHost(
                            navController = navController,
                            startDestination = Screen.Library.route
                        ) {
                            composable(Screen.Library.route) {
                                LibraryScreen(
                                    onOpenBook = { bookId ->
                                        navController.navigate("reader/$bookId")
                                    }
                                )
                            }

                            composable("reader/{bookId}") { backStackEntry ->
                                val bookId = backStackEntry.arguments?.getString("bookId")
                                    ?: return@composable

                                ReaderScreen(
                                    bookId = bookId,
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            composable(Screen.Stats.route) {
                                StatsScreen()
                            }

                            composable(Screen.Settings.route) {
                                SettingsScreen()
                            }
                        }
                    }
                }
            }
        }
    }
}
