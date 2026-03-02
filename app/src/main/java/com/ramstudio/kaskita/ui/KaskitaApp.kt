package com.ramstudio.kaskita.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import com.ramstudio.kaskita.AuthState
import com.ramstudio.kaskita.core.navigation.AppNavHost
import com.ramstudio.kaskita.core.utils.LocalAppSnackbarHostState
import com.ramstudio.kaskita.presentation.dashboard.DashboardViewModel
import com.ramstudio.kaskita.ui.component.BottomNavCurveShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KasKitaApp(
    appState: KasKitaState,
    authState: AuthState
) {
    if (authState is AuthState.Loading) return

    val dashboardViewModel: DashboardViewModel = hiltViewModel()
    val snackbarHostState = remember { SnackbarHostState() }
    val currentDestination = appState.currentDestination

    CompositionLocalProvider(LocalAppSnackbarHostState provides snackbarHostState) {
        val showBottomNav =
            currentDestination?.hierarchy?.any { dest ->
                appState.topLevelDestinations.any { item -> dest.hasRoute(item.route) }
            } == true

        val currentTopLevelDestination = appState.currentTopLevelDestination
        val dashboardUiState by dashboardViewModel.uiState.collectAsStateWithLifecycle()
        val selectedCommunityId = dashboardUiState.selectedCommunity?.id

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            floatingActionButtonPosition = FabPosition.Center,
            floatingActionButton = {
                AnimatedVisibility(
                    visible = showBottomNav,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    FloatingActionButton(
                        shape = CircleShape,
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        onClick = {
                            appState.navigateToAddTransactions(selectedCommunityId ?: "")

                        },
                        modifier = Modifier.offset(y = 48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Transaction"
                        )
                    }
                }
            },
            bottomBar = {
                AnimatedVisibility(
                    visible = showBottomNav,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer {
                                shape = BottomNavCurveShape()
                                clip = true
                                shadowElevation = 8.dp.toPx()
                            }
                    ) {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface
                        ) {
                            appState.topLevelDestinations.forEachIndexed { index, destination ->
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

                                if (index == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        ) { innerPadding ->
            AppNavHost(
                appState = appState,
                innerPadding = innerPadding,
                authState,
            )
        }
    }
}