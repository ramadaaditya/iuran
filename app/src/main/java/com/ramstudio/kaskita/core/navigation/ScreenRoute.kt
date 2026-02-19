package com.ramstudio.kaskita.core.navigation

import kotlinx.serialization.Serializable

sealed class RoutingNames {

    @Serializable
    object SignInRoute : RoutingNames()

    @Serializable
    object SignUpRoute : RoutingNames()

    @Serializable
    object DashboardRoute : RoutingNames()
}