package com.ramstudio.kaskita.core.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.ramstudio.kaskita.AuthState
import com.ramstudio.kaskita.presentation.auth.register.SignUpScreen
import com.ramstudio.kaskita.presentation.auth.signin.SignInScreen
import com.ramstudio.kaskita.presentation.community.CommunityScreen
import com.ramstudio.kaskita.presentation.dashboard.DashboardScreen
import com.ramstudio.kaskita.presentation.detailCommunity.CommunityDetailScreen
import com.ramstudio.kaskita.presentation.detailTransaction.TransactionDetailsScreen
import com.ramstudio.kaskita.presentation.detailTransaction.navigateToDetailTransaction
import com.ramstudio.kaskita.presentation.settings.SettingsScreen
import com.ramstudio.kaskita.presentation.transaction.AddTransactionScreen
import com.ramstudio.kaskita.presentation.transaction.TransactionScreen
import com.ramstudio.kaskita.ui.KasKitaState

@Composable
fun AppNavHost(
    appState: KasKitaState,
    innerPadding: PaddingValues,
    authState: AuthState
) {
    val navController = appState.navController

    when (authState) {
        AuthState.Loading -> {
        }

        AuthState.LoggedIn -> {
            NavHost(
                navController = navController,
                startDestination = ScreenRoute.DashboardRoute
            ) {
                composable<ScreenRoute.DashboardRoute> {
                    DashboardScreen(innerPadding, onTransactionClick = { id ->
                        navController.navigate(ScreenRoute.DetailTransaction(id))
                    })

                }
                composable<ScreenRoute.Community> {
                    CommunityScreen(
                        innerPadding = innerPadding,
                        onDetailClick = { communityId ->
                            navController.navigate(ScreenRoute.DetailCommunity(communityId))
                        }
                    )
                }

                composable<ScreenRoute.Transaction> {
                    TransactionScreen(innerPadding, onDetailClick = { id ->
                        navController.navigateToDetailTransaction(id)

                    })
                }
                composable<ScreenRoute.DetailTransaction> {
                    TransactionDetailsScreen(onBackClick = {}, transactionId = "")
                }
                composable<ScreenRoute.DetailCommunity> { backStackEntry ->
                    val route = backStackEntry.toRoute<ScreenRoute.DetailCommunity>()
                    CommunityDetailScreen(
                        communityId = route.communityId,
                        onBackClick = { navController.popBackStack() },
                        onAddTransactionClick = {
                            navController.navigate(ScreenRoute.AddTransactions)
                        }
                    )
                }
                composable<ScreenRoute.Settings> {
                    SettingsScreen(innerPadding)
                }

                composable<ScreenRoute.AddTransactions> {
                    AddTransactionScreen(onCloseClick = {})
                }
            }
        }

        AuthState.LoggedOut -> {
            NavHost(
                navController = navController,
                startDestination = ScreenRoute.SignInRoute,
            ) {
                composable<ScreenRoute.SignInRoute> {
                    SignInScreen(
                        onNavigateSignUp = {
                            navController.navigate(ScreenRoute.SignUpRoute) {
                                launchSingleTop = true
                            }
                        }
                    )
                }
                composable<ScreenRoute.SignUpRoute> {
                    SignUpScreen(
                        onNavigateSignIn = {
                            navController.popBackStack()
                        },
                    )
                }
            }
        }
    }
}

