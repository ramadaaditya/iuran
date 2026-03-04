package com.ramstudio.kaskita.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import com.ramstudio.kaskita.AuthState
import com.ramstudio.kaskita.core.navigation.AppNavHost
import com.ramstudio.kaskita.core.utils.LocalAppSnackbarHostState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KasKitaApp(
    appState: KasKitaState,
    authState: AuthState
) {
    if (authState is AuthState.Loading) return

    val snackbarHostState = remember { SnackbarHostState() }
    val currentDestination = appState.currentDestination

    CompositionLocalProvider(LocalAppSnackbarHostState provides snackbarHostState) {
        val showBottomNav =
            currentDestination?.hierarchy?.any { dest ->
                appState.topLevelDestinations.any { item -> dest.hasRoute(item.route) }
            } == true

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                AnimatedVisibility(
                    visible = showBottomNav,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface
                        ) {
                            appState.topLevelDestinations.forEach { destination ->
                                val selected =
                                    currentDestination.isRouteInHierarchy(destination.baseRoute)

                                NavigationBarItem(
                                    selected = selected,
                                    onClick = {
                                        appState.navigateToTopLevelDestination(destination)
                                    },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) destination.selectedIcon else destination.unselectedIcon,
                                            contentDescription = null
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.primary,
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurface,
                                        indicatorColor = Transparent
                                    )
                                )
                            }
                        }
                    }
                }
            }
        ) { innerPadding ->
            AppNavHost(
                appState = appState,
                innerPadding = innerPadding,
                authState = authState
            )
        }
    }
}
