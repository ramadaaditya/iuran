package com.ramstudio.kaskita.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.currentBackStackEntryAsState
import com.ramstudio.kaskita.core.navigation.ScreenRoute
//
//@Composable
//fun DynamicTopBar(navController: NavController) {
//    val entry by navController.currentBackStackEntryAsState()
//    val currentDestination = entry?.destination
//
//    if(currentDestination)
//}

fun NavBackStackEntry?.getDestiny(): ScreenRoute? {
    return this?.let {
        when {
            destination.hasRoute(ScreenRoute.DashboardRoute::class) -> ScreenRoute.DashboardRoute
            destination.hasRoute(ScreenRoute.Community::class) -> ScreenRoute.Community
            destination.hasRoute(ScreenRoute.Settings::class) -> ScreenRoute.Settings
            destination.hasRoute(ScreenRoute.Transaction::class) -> ScreenRoute.Transaction
            else -> null
        }
    }
}