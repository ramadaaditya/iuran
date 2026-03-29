package com.ramstudio.kaskita.core.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.ramstudio.kaskita.AuthState
import com.ramstudio.kaskita.core.ui.KasKitaState
import com.ramstudio.kaskita.presentation.auth.register.SignUpScreen
import com.ramstudio.kaskita.presentation.auth.signin.SignInScreen
import com.ramstudio.kaskita.presentation.community.CommunityScreen
import com.ramstudio.kaskita.presentation.dashboard.DashboardRouteScreen
import com.ramstudio.kaskita.presentation.detailCommunity.CommunityDetailScreen
import com.ramstudio.kaskita.presentation.detailCommunity.navigateToDetailCommunity
import com.ramstudio.kaskita.presentation.detailTransaction.TransactionDetailsScreen
import com.ramstudio.kaskita.presentation.settings.SettingsScreen
import com.ramstudio.kaskita.presentation.transaction.AddTransactionScreen
import com.ramstudio.kaskita.presentation.transaction.TransactionScreen
import com.ramstudio.kaskita.presentation.transaction.navigateToTransactions

@Composable
fun AppNavHost(
    appState: KasKitaState,
    innerPadding: PaddingValues,
    authState: AuthState,
    selectedCommunityId: String?,
    onSelectedCommunityChanged: (String?) -> Unit
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
                    DashboardRouteScreen(
                        innerPadding = innerPadding,
                        onTransactionClick = { id ->
                            navController.navigate(ScreenRoute.DetailTransaction(id))
                        },
                        onViewAllTransactionsClick = {
                            navController.navigateToTransactions()
                        },
                        selectedCommunityId = selectedCommunityId,
                        onSelectedCommunityChanged = onSelectedCommunityChanged
                    )

                }
                composable<ScreenRoute.Community> {
                    CommunityScreen(
                        innerPadding = innerPadding,
                        onDetailClick = { communityId ->
                            navController.navigateToDetailCommunity(communityId)
                        }
                    )
                }
                composable<ScreenRoute.Transaction> {
                    TransactionScreen(
                        innerPadding = innerPadding,
                        onDetailClick = { transactionId ->
                            navController.navigate(ScreenRoute.DetailTransaction(transactionId))
                        },
                        communityId = selectedCommunityId ?: "",
                        onAddTransactionClick = { isAdmin ->
                            val activeCommunityId = selectedCommunityId.orEmpty()
                            if (activeCommunityId.isNotBlank()) {
                                navController.navigate(
                                    ScreenRoute.AddTransactions(
                                        communityId = activeCommunityId,
                                        isAdmin = isAdmin
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
                        onEditClick = { communityId, isAdmin, transactionId ->
                            navController.navigate(
                                ScreenRoute.AddTransactions(
                                    communityId = communityId,
                                    isAdmin = isAdmin,
                                    editTransactionId = transactionId
                                )
                            )
                        },
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
                        editTransactionId = route.editTransactionId,
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
