package com.ramstudio.kaskita.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

object BottomBarIcons {

    val selectedDashboard: ImageVector = Icons.Filled.Dashboard
    val unselectedDashboard: ImageVector = Icons.Outlined.Dashboard

    val selectedCommunity: ImageVector = Icons.Filled.Groups
    val unselectedCommunity: ImageVector = Icons.Outlined.Groups

    val selectedTransaction: ImageVector = Icons.AutoMirrored.Filled.ReceiptLong
    val unselectedTransaction: ImageVector = Icons.AutoMirrored.Outlined.ReceiptLong

    val selectedSettings: ImageVector = Icons.Filled.Settings
    val unselectedSettings: ImageVector = Icons.Outlined.Settings
}