package com.ramstudio.kaskita.ui

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import com.ramstudio.kaskita.R
import com.ramstudio.kaskita.core.navigation.ScreenRoute
import kotlin.reflect.KClass

enum class TopLevelDestination(
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    @StringRes val iconTextId: Int,
    @StringRes val titleTextId: Int,
    val route: KClass<*>,
    val baseRoute: KClass<*> = route
) {
    DASHBOARD(
        selectedIcon = BottomBarIcons.selectedDashboard,
        unselectedIcon = BottomBarIcons.unselectedDashboard,
        iconTextId = R.string.Dashboard,
        titleTextId = R.string.Dashboard,
        route = ScreenRoute.DashboardRoute::class,
    ),
    COMMUNITY(
        selectedIcon = BottomBarIcons.selectedCommunity,
        unselectedIcon = BottomBarIcons.unselectedCommunity,
        iconTextId = R.string.Community,
        titleTextId = R.string.Community,
        route = ScreenRoute.Community::class,
    ),
    TRANSACTIONS(
        selectedIcon = BottomBarIcons.selectedTransaction,
        unselectedIcon = BottomBarIcons.unselectedTransaction,
        iconTextId = R.string.Transaction,
        titleTextId = R.string.Transaction,
        route = ScreenRoute.Transaction::class,
    ),
    SETTINGS(
        selectedIcon = BottomBarIcons.selectedSettings,
        unselectedIcon = BottomBarIcons.unselectedSettings,
        iconTextId = R.string.Settings,
        titleTextId = R.string.Settings,
        route = ScreenRoute.Settings::class,
    ),
}
