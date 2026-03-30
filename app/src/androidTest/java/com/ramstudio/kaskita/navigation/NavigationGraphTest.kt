package com.ramstudio.kaskita.navigation

import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import androidx.navigation.toRoute
import com.ramstudio.kaskita.core.navigation.ScreenRoute
import com.ramstudio.kaskita.core.navigation.TopLevelDestination
import com.ramstudio.kaskita.core.ui.KasKitaState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class NavigationGraphTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var navController: TestNavHostController

    @Before
    fun setup() {
        // Content is set per-test to avoid double setContent calls.
    }

    @Test
    fun mainGraph_startDestination_is_dashboard() {
        setMainGraphContent()
        composeTestRule.runOnIdle {
            assertTrue(
                navController.currentDestination?.hasRoute(ScreenRoute.DashboardRoute::class) == true
            )
        }
    }

    @Test
    fun mainGraph_can_navigate_to_top_level_destinations() {
        setMainGraphContent()
        val appState = KasKitaState(navController)

        composeTestRule.runOnIdle {
            appState.navigateToTopLevelDestination(TopLevelDestination.COMMUNITY)
            assertTrue(
                navController.currentDestination?.hasRoute(ScreenRoute.Community::class) == true
            )

            appState.navigateToTopLevelDestination(TopLevelDestination.TRANSACTIONS)
            assertTrue(
                navController.currentDestination?.hasRoute(ScreenRoute.Transaction::class) == true
            )

            appState.navigateToTopLevelDestination(TopLevelDestination.SETTINGS)
            assertTrue(
                navController.currentDestination?.hasRoute(ScreenRoute.Settings::class) == true
            )

            appState.navigateToTopLevelDestination(TopLevelDestination.DASHBOARD)
            assertTrue(
                navController.currentDestination?.hasRoute(ScreenRoute.DashboardRoute::class) == true
            )
        }
    }

    @Test
    fun mainGraph_can_navigate_to_detail_transaction_and_back() {
        setMainGraphContent()
        val transactionId = "tx-123"

        composeTestRule.runOnIdle {
            navController.navigate(ScreenRoute.DetailTransaction(transactionId))
        }

        composeTestRule.runOnIdle {
            val route = navController.currentBackStackEntry
                ?.toRoute<ScreenRoute.DetailTransaction>()
            assertEquals(transactionId, route?.transactionId)
        }

        composeTestRule.runOnIdle {
            navController.popBackStack()
            assertTrue(
                navController.currentDestination?.hasRoute(ScreenRoute.DashboardRoute::class) == true
            )
        }
    }

    @Test
    fun mainGraph_can_navigate_to_detail_community_and_add_transactions() {
        setMainGraphContent()
        val communityId = "comm-1"

        composeTestRule.runOnIdle {
            navController.navigate(ScreenRoute.DetailCommunity(communityId))
        }

        composeTestRule.runOnIdle {
            val route = navController.currentBackStackEntry
                ?.toRoute<ScreenRoute.DetailCommunity>()
            assertEquals(communityId, route?.communityId)
        }

        val editTransactionId = "tx-999"
        composeTestRule.runOnIdle {
            navController.navigate(
                ScreenRoute.AddTransactions(
                    communityId = communityId,
                    isAdmin = true,
                    editTransactionId = editTransactionId
                )
            )
        }

        composeTestRule.runOnIdle {
            val route = navController.currentBackStackEntry
                ?.toRoute<ScreenRoute.AddTransactions>()
            assertEquals(communityId, route?.communityId)
            assertEquals(true, route?.isAdmin)
            assertEquals(editTransactionId, route?.editTransactionId)
        }
    }

    @Test
    fun authGraph_can_navigate_signin_to_signup_and_back() {
        setAuthGraphContent()

        composeTestRule.runOnIdle {
            assertTrue(
                navController.currentDestination?.hasRoute(ScreenRoute.SignInRoute::class) == true
            )
        }

        composeTestRule.runOnIdle {
            navController.navigate(ScreenRoute.SignUpRoute)
        }

        composeTestRule.runOnIdle {
            assertTrue(
                navController.currentDestination?.hasRoute(ScreenRoute.SignUpRoute::class) == true
            )
        }

        composeTestRule.runOnIdle {
            navController.popBackStack()
            assertTrue(
                navController.currentDestination?.hasRoute(ScreenRoute.SignInRoute::class) == true
            )
        }
    }

    private fun setMainGraphContent() {
        composeTestRule.setContent {
            navController = TestNavHostController(LocalContext.current).apply {
                navigatorProvider.addNavigator(ComposeNavigator())
            }
            TestMainNavGraph(navController)
        }
    }

    private fun setAuthGraphContent() {
        composeTestRule.setContent {
            navController = TestNavHostController(LocalContext.current).apply {
                navigatorProvider.addNavigator(ComposeNavigator())
            }
            TestAuthNavGraph(navController)
        }
    }
}

@Composable
private fun TestMainNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = ScreenRoute.DashboardRoute
    ) {
        composable<ScreenRoute.DashboardRoute> { Text("Dashboard") }
        composable<ScreenRoute.Community> { Text("Community") }
        composable<ScreenRoute.Transaction> { Text("Transaction") }
        composable<ScreenRoute.DetailTransaction> { Text("DetailTransaction") }
        composable<ScreenRoute.DetailCommunity> { Text("DetailCommunity") }
        composable<ScreenRoute.Settings> { Text("Settings") }
        composable<ScreenRoute.AddTransactions> { Text("AddTransactions") }
    }
}

@Composable
private fun TestAuthNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = ScreenRoute.SignInRoute
    ) {
        composable<ScreenRoute.SignInRoute> { Text("SignIn") }
        composable<ScreenRoute.SignUpRoute> { Text("SignUp") }
    }
}
