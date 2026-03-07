package com.ramstudio.kaskita.core.navigation

import kotlinx.serialization.Serializable


sealed interface RootGraph {
    data object AuthGraph : RootGraph
    data object MainGraph : RootGraph
}

sealed class ScreenRoute {

    @Serializable
    object SignInRoute : ScreenRoute()

    @Serializable
    object SignUpRoute : ScreenRoute()

    @Serializable
    object DashboardRoute : ScreenRoute()
    @Serializable
    object Community : ScreenRoute()
    @Serializable
    data class DetailCommunity(val communityId: String) : ScreenRoute()

    @Serializable
    object Transaction : ScreenRoute()

    @Serializable
    object Settings : ScreenRoute()

    @Serializable
    object Splash : ScreenRoute()

    @Serializable
    data class AddTransactions(val communityId: String, val isAdmin: Boolean = false) : ScreenRoute()

    @Serializable
    data class DetailTransaction(val transactionId: String) : ScreenRoute()
}