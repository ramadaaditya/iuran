package com.ramstudio.kaskita.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.util.trace
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import com.ramstudio.kaskita.core.navigation.ScreenRoute
import com.ramstudio.kaskita.presentation.community.navigateToCommunity
import com.ramstudio.kaskita.presentation.dashboard.navigateToDashboard
import com.ramstudio.kaskita.presentation.settings.navigateToSettings
import com.ramstudio.kaskita.presentation.transaction.navigateToTransactions
import kotlin.reflect.KClass

@Composable
fun rememberKaskitaState(
    navController: NavHostController = rememberNavController()
): KasKitaState {
    return remember(navController) {
        KasKitaState(
            navController = navController
        )
    }
}

@Stable
class KasKitaState(
    val navController: NavHostController,
) {
    private val previousDestination = mutableStateOf<NavDestination?>(null)
    val currentDestination: NavDestination?
        @Composable get() {
            val currentEntry = navController.currentBackStackEntryFlow
                .collectAsState(initial = null)

            return currentEntry.value?.destination.also { destination ->
                if (destination != null) {
                    previousDestination.value = destination
                }
            } ?: previousDestination.value
        }

    val currentTopLevelDestination: TopLevelDestination?
        @Composable get() {
            return TopLevelDestination.entries.firstOrNull { topLevelDestination ->
                currentDestination.isRouteInHierarchy(topLevelDestination.baseRoute)
            }
        }
    val topLevelDestinations: List<TopLevelDestination> = TopLevelDestination.entries


    fun navigateToTopLevelDestination(topLevelDestination: TopLevelDestination) {
        trace("Navigation : ${topLevelDestination.name}") {
            val topLevelNavOptions = navOptions {

                popUpTo<ScreenRoute.DashboardRoute> {
                    saveState = true
                }

                launchSingleTop = true
                restoreState = true
            }

            when (topLevelDestination) {
                TopLevelDestination.DASHBOARD -> navController.navigateToDashboard(
                    topLevelNavOptions
                )

                TopLevelDestination.COMMUNITY -> navController.navigateToCommunity(
                    topLevelNavOptions
                )

                TopLevelDestination.TRANSACTIONS -> navController.navigateToTransactions(
                    topLevelNavOptions
                )

                TopLevelDestination.SETTINGS -> navController.navigateToSettings(
                    topLevelNavOptions
                )
            }
        }
    }

    fun navigateToAddTransactions(communityId: String, isAdmin: Boolean = false) =
        navController.navigate(ScreenRoute.AddTransactions(communityId, isAdmin))


}

fun NavDestination?.isRouteInHierarchy(route: KClass<*>): Boolean {
    return this?.hierarchy?.any { it.hasRoute(route) } == true
}
