package com.ramstudio.kaskita.core.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.ramstudio.kaskita.AuthState
import com.ramstudio.kaskita.presentation.auth.register.SignUpScreen
import com.ramstudio.kaskita.presentation.auth.signin.SignInScreen
import com.ramstudio.kaskita.presentation.community.CommunityScreen
import com.ramstudio.kaskita.presentation.dashboard.DashboardScreen
import com.ramstudio.kaskita.presentation.dashboard.DashboardViewModel
import com.ramstudio.kaskita.presentation.detailCommunity.CommunityDetailScreen
import com.ramstudio.kaskita.presentation.detailTransaction.TransactionDetailsScreen
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
                    DashboardScreen(
                        innerPadding = innerPadding,
                        onTransactionClick = { id ->
                            navController.navigate(ScreenRoute.DetailTransaction(id))
                        }
                    )

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
                    val dashboardViewModel: DashboardViewModel = hiltViewModel()
                    val dashboardUiState by dashboardViewModel.uiState.collectAsStateWithLifecycle()

                    TransactionScreen(
                        innerPadding = innerPadding,
                        onDetailClick = { transactionId ->
                            navController.navigate(ScreenRoute.DetailTransaction(transactionId))
                        },
                        isAdmin = dashboardUiState.isAdmin,
                        communityId = dashboardUiState.selectedCommunity?.id ?: "",
                        onAddTransactionClick = {
                            val selectedCommunityId = dashboardUiState.selectedCommunity?.id.orEmpty()
                            if (selectedCommunityId.isNotBlank()) {
                                navController.navigate(
                                    ScreenRoute.AddTransactions(
                                        communityId = selectedCommunityId,
                                        isAdmin = dashboardUiState.isAdmin
                                    )
                                )
                            }
                        }
                    )
                }
                composable<ScreenRoute.DetailTransaction> { backStackEntry ->
                    val route = backStackEntry.toRoute<ScreenRoute.DetailTransaction>()
                    TransactionDetailsScreen(
                        onBackClick = { navController.popBackStack() },
                        transactionId = route.transactionId
                    )
                }
                composable<ScreenRoute.DetailCommunity> { backStackEntry ->
                    val route = backStackEntry.toRoute<ScreenRoute.DetailCommunity>()
                    CommunityDetailScreen(
                        communityId = route.communityId,
                        onBackClick = { navController.popBackStack() },
                        onAddTransactionClick = { isAdmin ->
                            navController.navigate(
                                ScreenRoute.AddTransactions(
                                    communityId = route.communityId,
                                    isAdmin = isAdmin
                                )
                            )
                        }
                    )
                }
                composable<ScreenRoute.Settings> {
                    SettingsScreen(innerPadding)
                }

                composable<ScreenRoute.AddTransactions> { backStackEntry ->
                    val route = backStackEntry.toRoute<ScreenRoute.AddTransactions>()
                    AddTransactionScreen(
                        communityId = route.communityId,
                        isAdmin = route.isAdmin,
                        onCloseClick = { navController.popBackStack() },
                        onSuccess = { navController.popBackStack() }
                    )
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
