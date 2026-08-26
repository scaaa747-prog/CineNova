package com.cinenova.app.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.cinenova.app.data.DemoRepository
import com.cinenova.app.ui.screens.ContinueWatchingScreen
import com.cinenova.app.ui.screens.DetailsScreen
import com.cinenova.app.ui.screens.DownloadsScreen
import com.cinenova.app.ui.screens.ExploreScreen
import com.cinenova.app.ui.screens.HomeScreen
import com.cinenova.app.ui.screens.NotificationsScreen
import com.cinenova.app.ui.screens.PlayerScreen
import com.cinenova.app.ui.screens.ProfileScreen
import com.cinenova.app.ui.screens.SearchScreen
import com.cinenova.app.ui.screens.WatchlistScreen

object Routes {
    const val HOME = "home"
    const val EXPLORE = "explore"
    const val SEARCH = "search"
    const val DOWNLOADS = "downloads"
    const val WATCHLIST = "watchlist"
    const val PROFILE = "profile"
    const val DETAILS = "details/{itemId}"
    const val PLAYER = "player/{itemId}"
    const val CONTINUE_WATCHING = "continue-watching"
    const val NOTIFICATIONS = "notifications"

    fun details(itemId: String) = "details/$itemId"
    fun player(itemId: String) = "player/$itemId"
}

private data class BottomDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector,
)

private val bottomDestinations = listOf(
    BottomDestination(Routes.HOME, "Home", Icons.Outlined.Home, Icons.Filled.Home),
    BottomDestination(Routes.EXPLORE, "Explore", Icons.Outlined.Explore, Icons.Filled.Explore),
    BottomDestination(Routes.DOWNLOADS, "Downloads", Icons.Outlined.Download, Icons.Filled.DownloadDone),
    BottomDestination(Routes.WATCHLIST, "Watchlist", Icons.Outlined.BookmarkBorder, Icons.Filled.Bookmark),
    BottomDestination(Routes.PROFILE, "Profile", Icons.Outlined.Person, Icons.Filled.Person),
)

private val tabRoutes = bottomDestinations.map { it.route }.toSet()

/**
 * Adaptive app shell: NavigationBar on compact widths, NavigationRail on
 * medium/expanded (tablet & desktop).
 */
@Composable
fun CineNovaApp() {
    androidx.compose.runtime.LaunchedEffect(Unit) {
        com.cinenova.app.di.ServiceLocator.catalogRepository.bootstrap()
    }

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val adaptiveInfo = currentWindowAdaptiveInfo()
    val isExpanded = adaptiveInfo.windowSizeClass.windowWidthSizeClass ==
        androidx.window.core.layout.WindowWidthSizeClass.EXPANDED ||
        adaptiveInfo.windowSizeClass.windowWidthSizeClass ==
        androidx.window.core.layout.WindowWidthSizeClass.MEDIUM

    val showBars = currentRoute in tabRoutes

    if (isExpanded && showBars) {
        Row(Modifier.fillMaxSize()) {
            NavigationRail(
                modifier = Modifier
                    .fillMaxHeight()
                    .widthIn(min = 80.dp),
            ) {
                bottomDestinations.forEach { dest ->
                    val selected = currentRoute == dest.route
                    NavigationRailItem(
                        selected = selected,
                        onClick = { navigateToTab(navController, dest.route) },
                        icon = {
                            Icon(
                                if (selected) dest.selectedIcon else dest.icon,
                                contentDescription = dest.label,
                            )
                        },
                        label = { Text(dest.label) },
                    )
                }
            }
            Box(Modifier.weight(1f)) {
                AppNavHost(navController, currentRoute)
            }
        }
    } else {
        Scaffold(
            bottomBar = {
                if (showBars) {
                    NavigationBar {
                        bottomDestinations.forEach { dest ->
                            val selected = currentRoute == dest.route
                            NavigationBarItem(
                                selected = selected,
                                onClick = { navigateToTab(navController, dest.route) },
                                icon = {
                                    Icon(
                                        if (selected) dest.selectedIcon else dest.icon,
                                        contentDescription = dest.label,
                                    )
                                },
                                label = { Text(dest.label) },
                            )
                        }
                    }
                }
            },
        ) { innerPadding ->
            Box(Modifier.padding(innerPadding)) {
                AppNavHost(navController, currentRoute)
            }
        }
    }
}

private fun navigateToTab(
    navController: androidx.navigation.NavHostController,
    route: String,
) {
    navController.navigate(route) {
        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
private fun AppNavHost(
    navController: androidx.navigation.NavHostController,
    currentRoute: String?,
) {
    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(
                onOpenSearch = { navController.navigate(Routes.SEARCH) },
                onOpenDetails = { navController.navigate(Routes.details(it)) },
                onPlay = { navController.navigate(Routes.player(it)) },
                onOpenNotifications = { navController.navigate(Routes.NOTIFICATIONS) },
                onOpenContinueWatching = { navController.navigate(Routes.CONTINUE_WATCHING) },
            )
        }
        composable(Routes.EXPLORE) {
            ExploreScreen(onOpenDetails = { navController.navigate(Routes.details(it)) })
        }
        composable(Routes.SEARCH) {
            SearchScreen(
                onBack = { navController.popBackStack() },
                onOpenDetails = { navController.navigate(Routes.details(it)) },
            )
        }
        composable(Routes.DOWNLOADS) {
            DownloadsScreen(
                onBack = { navController.popBackStack() },
                onOpenDetails = { navController.navigate(Routes.details(it)) },
                onExplore = { navigateToTab(navController, Routes.EXPLORE) },
            )
        }
        composable(Routes.WATCHLIST) {
            WatchlistScreen(
                onOpenDetails = { navController.navigate(Routes.details(it)) },
                onExplore = { navigateToTab(navController, Routes.EXPLORE) },
            )
        }
        composable(Routes.PROFILE) {
            ProfileScreen(onManageDownloads = { navigateToTab(navController, Routes.DOWNLOADS) })
        }
        composable(Routes.DETAILS) { entry ->
            val itemId = entry.arguments?.getString("itemId") ?: return@composable
            DetailsScreen(
                itemId = itemId,
                onBack = { navController.popBackStack() },
                onPlay = { navController.navigate(Routes.player(it)) },
            )
        }
        composable(Routes.PLAYER) { entry ->
            val itemId = entry.arguments?.getString("itemId") ?: return@composable
            PlayerScreen(
                itemId = itemId,
                onBack = { navController.popBackStack() },
            )
        }
        composable(Routes.CONTINUE_WATCHING) {
            ContinueWatchingScreen(
                onBack = { navController.popBackStack() },
                onPlay = { navController.navigate(Routes.player(it)) },
                onOpenDetails = { navController.navigate(Routes.details(it)) },
            )
        }
        composable(Routes.NOTIFICATIONS) {
            NotificationsScreen(
                onBack = { navController.popBackStack() },
                onOpenDetails = { navController.navigate(Routes.details(it)) },
            )
        }
    }
}
