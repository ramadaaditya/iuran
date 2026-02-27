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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import com.ramstudio.kaskita.AuthState
import com.ramstudio.kaskita.core.navigation.AppNavHost
import com.ramstudio.kaskita.ui.component.BottomNavCurveShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KasKitaApp(
    appState: KasKitaState,
    authState: AuthState
) {
    if (authState is AuthState.Loading) return

    val snackbarHostState = remember { SnackbarHostState() }
    val currentDestination = appState.currentDestination

//    CompositionLocalProvider(LocalAppSnackbarHostState provides snackbarHostState) {
//        LaunchedEffect(Unit) {
//            appState.isConnected.collectLatest { isConnected ->
//
//                if (isOnSplashScreen) return@collectLatest
//                if (isConnected) {
//                    snackbarHostState.currentSnackbarData?.dismiss()
//                    snackbarHostState.showSnackbar(
//                        message = "Koneksi kembali normal",
//                        duration = SnackbarDuration.Short
//                    )
//                } else {
//                    snackbarHostState.showSnackbar(
//                        message = "Tidak ada koneksi internet",
//                        withDismissAction = false,
//                        duration = SnackbarDuration.Long
//                    )
//                }
//            }
//        }


    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    val showBottomNav =
        currentDestination?.hierarchy?.any { dest ->
            appState.topLevelDestinations.any { item -> dest.hasRoute(item.route) }
        } == true

    val currentTopLevelDestination = appState.currentTopLevelDestination

    Scaffold(
//        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
//        topBar = {
//            AnimatedVisibility(
//                visible = showBottomNav && currentTopLevelDestination != null,
//                enter = slideInVertically { -it } + fadeIn(),
//                exit = slideOutVertically { -it } + fadeOut()
//            ) {
//                TopAppBar(
//                    title = {
//                        // Mengambil titleTextId dinamis dari Enum Anda
//                        currentTopLevelDestination?.let { dest ->
//                            Text(
//                                text = stringResource(id = dest.titleTextId),
//                                fontWeight = FontWeight.Bold
//                            )
//                        }
//                    },
//                    scrollBehavior = scrollBehavior, // Memasang scroll behavior ke TopAppBar
//                    colors = TopAppBarDefaults.topAppBarColors(
//                        containerColor = MaterialTheme.colorScheme.background,
//                        scrolledContainerColor = MaterialTheme.colorScheme.surface // Warna saat terscroll
//                    )
//                )
//            }
//        },
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
                        appState.navigateToAddTransactions()

                    },
                    modifier = Modifier.offset(y = 48.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Transaction")
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