package com.badgr.orbreader

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.badgr.orbreader.data.preferences.THEME_DARK
import com.badgr.orbreader.data.preferences.THEME_LIGHT
import com.badgr.orbreader.data.preferences.UserPreferencesRepository
import com.badgr.orbreader.ui.library.LibraryScreen
import com.badgr.orbreader.ui.reader.ReaderScreen
import com.badgr.orbreader.ui.stats.StatsScreen
import com.badgr.orbreader.ui.settings.SettingsScreen
import com.badgr.orbreader.ui.account.AccountScreen
import com.badgr.orbreader.ui.theme.OrbreaderTheme
import kotlinx.coroutines.launch

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    data object Library  : Screen("library",  "Library",  Icons.Default.CollectionsBookmark)
    data object Stats    : Screen("stats",    "Stats",    Icons.Default.QueryStats)
    data object Settings : Screen("settings", "Settings", Icons.Default.Settings)
    data object Account  : Screen("account",  "Account",  Icons.Default.Person)
}

class MainActivity : ComponentActivity() {

    private lateinit var prefsRepo: UserPreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefsRepo = UserPreferencesRepository(this)
        enableEdgeToEdge()
        setContent {
            val prefs by prefsRepo.preferences.collectAsStateWithLifecycle(
                initialValue = com.badgr.orbreader.data.preferences.UserPreferences()
            )
            val systemDark = isSystemInDarkTheme()
            val useDark = when (prefs.themeMode) {
                THEME_LIGHT -> false
                THEME_DARK  -> true
                else        -> systemDark
            }

            OrbreaderTheme(darkTheme = useDark) {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                val showBottomNav = currentDestination?.route?.startsWith("reader") != true

                Scaffold(
                    bottomBar = {
                        if (showBottomNav) {
                            NavigationBar {
                                val items = listOf(Screen.Library, Screen.Stats, Screen.Settings, Screen.Account)
                                items.forEach { screen ->
                                    val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                                    NavigationBarItem(
                                        icon     = { Icon(screen.icon, contentDescription = null) },
                                        label    = { Text(screen.label) },
                                        selected = isSelected,
                                        onClick  = {
                                            navController.navigate(screen.route) {
                                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    Surface(
                        modifier = Modifier.padding(innerPadding),
                        color    = MaterialTheme.colorScheme.background
                    ) {
                        NavHost(
                            navController    = navController,
                            startDestination = Screen.Library.route
                        ) {
                            composable(Screen.Library.route) {
                                LibraryScreen(onOpenBook = { bookId ->
                                    navController.navigate("reader/$bookId")
                                })
                            }
                            composable("reader/{bookId}") { backStackEntry ->
                                val bookId = backStackEntry.arguments?.getString("bookId")
                                    ?: return@composable
                                ReaderScreen(bookId = bookId, onBack = { navController.popBackStack() })
                            }
                            composable(Screen.Stats.route)    { StatsScreen() }
                            composable(Screen.Settings.route) { SettingsScreen() }
                            composable(Screen.Account.route)  { AccountScreen() }
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val purchaseManager = (application as OrbReaderApp).purchaseManager
        if (purchaseManager.isConnected.value) {
            lifecycleScope.launch { purchaseManager.queryExistingPurchases() }
        }
    }
}
