package com.ramstudio.kaskita.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import com.ramstudio.kaskita.AuthState
import com.ramstudio.kaskita.core.navigation.AppNavHost
import com.ramstudio.kaskita.core.utils.LocalAppSnackbarHostState
import com.ramstudio.kaskita.presentation.onboarding.OnboardingScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KasKitaApp(
    appState: KasKitaState,
    authState: AuthState,
    showOnboarding: Boolean,
    onOnboardingFinished: () -> Unit
) {
    if (authState is AuthState.Loading) return
    if (showOnboarding) {
        OnboardingScreen(onFinish = onOnboardingFinished)
        return
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val currentDestination = appState.currentDestination
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val currentTopLevelDestination = appState.currentTopLevelDestination


    CompositionLocalProvider(LocalAppSnackbarHostState provides snackbarHostState) {
        val showBottomNav =
            currentDestination?.hierarchy?.any { dest ->
                appState.topLevelDestinations.any { item -> dest.hasRoute(item.route) }
            } == true

        Scaffold(
            topBar = {
                AnimatedVisibility(
                    visible = showBottomNav,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    CenterAlignedTopAppBar(
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background,
                            titleContentColor = MaterialTheme.colorScheme.onBackground,
                        ),
                        title = {
                            Text(
                                currentTopLevelDestination?.titleTextId
                                    ?.let { stringResource(it) }
                                    ?: "",
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold
                            )
                        },
                        scrollBehavior = scrollBehavior,
                    )
                }
            },

            containerColor = MaterialTheme.colorScheme.background,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                AnimatedVisibility(
                    visible = showBottomNav,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        HorizontalDivider(
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.outline
                        )
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
