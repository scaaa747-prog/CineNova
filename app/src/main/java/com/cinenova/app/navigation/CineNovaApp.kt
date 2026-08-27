package com.cinenova.app.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.cinenova.app.data.AppStore
import com.cinenova.app.di.ServiceLocator
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
    const val MAIN_TABS = "main_tabs"
    const val SEARCH = "search"
    const val NOTIFICATIONS = "notifications"
    const val CONTINUE_WATCHING = "continue_watching"
    const val DETAILS = "details/{itemId}"
    const val PLAYER = "player/{itemId}"

    fun details(id: String) = "details/$id"
    fun player(id: String) = "player/$id"
}

private data class BottomDestination(
    val index: Int,
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector,
)

private val bottomDestinations = listOf(
    BottomDestination(0, "Home", Icons.Outlined.Home, Icons.Filled.Home),
    BottomDestination(1, "Explore", Icons.Outlined.Explore, Icons.Filled.Explore),
    BottomDestination(2, "Downloads", Icons.Outlined.Download, Icons.Filled.DownloadDone),
    BottomDestination(3, "Watchlist", Icons.Outlined.BookmarkBorder, Icons.Filled.Bookmark),
    BottomDestination(4, "Profile", Icons.Outlined.Person, Icons.Filled.Person),
)

/**
 * 60FPS Ultra-Responsive CineNova app with Sliding Pill Glassmorphic Navigation Bar.
 */
@Composable
fun CineNovaApp() {
    LaunchedEffect(Unit) {
        ServiceLocator.catalogRepository.bootstrap()
    }

    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.MAIN_TABS) {
        composable(Routes.MAIN_TABS) {
            MainTabsContainer(
                onOpenSearch = { navController.navigate(Routes.SEARCH) },
                onOpenDetails = { navController.navigate(Routes.details(it)) },
                onPlay = { navController.navigate(Routes.player(it)) },
                onOpenNotifications = { navController.navigate(Routes.NOTIFICATIONS) },
                onOpenContinueWatching = { navController.navigate(Routes.CONTINUE_WATCHING) },
            )
        }
        composable(Routes.SEARCH) {
            SearchScreen(
                onBack = { navController.popBackStack() },
                onOpenDetails = { navController.navigate(Routes.details(it)) },
            )
        }
        composable(Routes.DETAILS) { entry ->
            val itemId = entry.arguments?.getString("itemId") ?: return@composable
            DetailsScreen(
                itemId = itemId,
                onBack = { navController.popBackStack() },
                onPlay = { navController.navigate(Routes.player(it)) },
                onOpenDetails = { navController.navigate(Routes.details(it)) },
                onNavigateDownloads = { navController.popBackStack() },
            )
        }
        composable(Routes.PLAYER) { entry ->
            val itemId = entry.arguments?.getString("itemId") ?: return@composable
            PlayerScreen(
                itemId = itemId,
                onBack = { navController.popBackStack() },
            )
        }
        composable(Routes.NOTIFICATIONS) {
            NotificationsScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.CONTINUE_WATCHING) {
            ContinueWatchingScreen(
                onBack = { navController.popBackStack() },
                onResume = { navController.navigate(Routes.player(it)) },
                onOpenDetails = { navController.navigate(Routes.details(it)) },
            )
        }
    }
}

/**
 * Instant 0ms Zero-Lag Tab Container with smooth Crossfade transitions.
 */
@Composable
private fun MainTabsContainer(
    onOpenSearch: () -> Unit,
    onOpenDetails: (String) -> Unit,
    onPlay: (String) -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenContinueWatching: () -> Unit,
) {
    var selectedIndex by remember { mutableIntStateOf(0) }
    val useGlassNav = AppStore.glassNavBar.value

    if (useGlassNav) {
        Box(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            Crossfade(
                targetState = selectedIndex,
                animationSpec = spring(stiffness = Spring.StiffnessMedium),
                label = "tabCrossfade",
            ) { page ->
                when (page) {
                    0 -> HomeScreen(
                        onOpenSearch = onOpenSearch,
                        onOpenDetails = onOpenDetails,
                        onPlay = onPlay,
                        onOpenNotifications = onOpenNotifications,
                        onOpenContinueWatching = onOpenContinueWatching,
                    )
                    1 -> ExploreScreen(onOpenDetails = onOpenDetails)
                    2 -> DownloadsScreen(
                        onBack = { selectedIndex = 0 },
                        onOpenDetails = onOpenDetails,
                        onExplore = { selectedIndex = 1 },
                    )
                    3 -> WatchlistScreen(
                        onOpenDetails = onOpenDetails,
                        onExplore = { selectedIndex = 1 },
                    )
                    4 -> ProfileScreen(onManageDownloads = { selectedIndex = 2 })
                }
            }

            // Floating elevated glassmorphic pill bar with sliding indicator
            FloatingGlassNavBar(
                selectedIndex = selectedIndex,
                onSelectIndex = { selectedIndex = it },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 20.dp, vertical = 24.dp),
            )
        }
    } else {
        Scaffold(
            bottomBar = {
                NavigationBar {
                    bottomDestinations.forEach { dest ->
                        val selected = selectedIndex == dest.index
                        NavigationBarItem(
                            selected = selected,
                            onClick = { selectedIndex = dest.index },
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
            },
        ) { innerPadding ->
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background),
            ) {
                Crossfade(
                    targetState = selectedIndex,
                    animationSpec = spring(stiffness = Spring.StiffnessMedium),
                    label = "tabCrossfadeStandard",
                ) { page ->
                    when (page) {
                        0 -> HomeScreen(
                            onOpenSearch = onOpenSearch,
                            onOpenDetails = onOpenDetails,
                            onPlay = onPlay,
                            onOpenNotifications = onOpenNotifications,
                            onOpenContinueWatching = onOpenContinueWatching,
                        )
                        1 -> ExploreScreen(onOpenDetails = onOpenDetails)
                        2 -> DownloadsScreen(
                            onBack = { selectedIndex = 0 },
                            onOpenDetails = onOpenDetails,
                            onExplore = { selectedIndex = 1 },
                        )
                        3 -> WatchlistScreen(
                            onOpenDetails = onOpenDetails,
                            onExplore = { selectedIndex = 1 },
                        )
                        4 -> ProfileScreen(onManageDownloads = { selectedIndex = 2 })
                    }
                }
            }
        }
    }
}

/**
 * Floating glassmorphism pill with sliding active indicator.
 */
@Composable
private fun FloatingGlassNavBar(
    selectedIndex: Int,
    onSelectIndex: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 16.dp,
                shape = CircleShape,
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
            )
            .clip(CircleShape)
            .border(
                border = BorderStroke(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.30f),
                            Color.White.copy(alpha = 0.06f),
                        ),
                    ),
                ),
                shape = CircleShape,
            ),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
        tonalElevation = 6.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            bottomDestinations.forEach { dest ->
                val selected = selectedIndex == dest.index
                val iconColor by animateColorAsState(
                    targetValue = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    label = "navIconColor",
                )

                Column(
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onSelectIndex(dest.index) },
                        )
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.50f)
                                else Color.Transparent,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = if (selected) dest.selectedIcon else dest.icon,
                            contentDescription = dest.label,
                            tint = iconColor,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = dest.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = iconColor,
                    )
                }
            }
        }
    }
}
