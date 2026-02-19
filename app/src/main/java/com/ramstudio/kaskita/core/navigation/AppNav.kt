package com.ramstudio.kaskita.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.ramstudio.kaskita.presentation.auth.register.SignUpScreen
import com.ramstudio.kaskita.presentation.auth.signin.SignInScreen
import com.ramstudio.kaskita.presentation.dashboard.DashboardScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    isUserLoggedIn: Boolean
) {
    val startDestination =
        if (isUserLoggedIn) RoutingNames.DashboardRoute else RoutingNames.SignInRoute
    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {
//        composable<RoutingNames.RootRoute> {
//            when (isUserLoggedIn) {
//                true -> {
//                    navController.navigate(RoutingNames.DashboardRoute) {
//                        popUpTo(RoutingNames.RootRoute) { inclusive = true }
//                    }
//                }
//
//                false -> {
//                    navController.navigate(RoutingNames.SignInRoute) {
//
//                        popUpTo(RoutingNames.RootRoute) { inclusive = true }
//                    }
//                }
//            }
//        }

        composable<RoutingNames.SignInRoute> {
            SignInScreen(
                onNavigateDashboard = {
                    navController.navigate(RoutingNames.DashboardRoute) {
                        popUpTo(RoutingNames.SignInRoute) { inclusive = true }
                    }
                },

                onNavigateSignUp = {
                    navController.navigate(RoutingNames.SignUpRoute) {
                        launchSingleTop = true
                    }
                }
            )
        }
        composable<RoutingNames.SignUpRoute> {
            SignUpScreen(
                onNavigateSignIn = {
                    navController.popBackStack()
                },
            )
        }
        composable<RoutingNames.DashboardRoute> {
            DashboardScreen()
        }
    }
}

