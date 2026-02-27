package com.ramstudio.kaskita.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.ramstudio.kaskita.core.navigation.ScreenRoute
import com.ramstudio.kaskita.presentation.settings.component.LogoutConfirmationDialog
import com.ramstudio.kaskita.presentation.settings.component.ProfileCard

val TextMainBlack = Color(0xFF1A1A1A)
val TextSubGrey = Color(0xFF8E8E93)
val BackgroundLightGrey = Color(0xFFF5F6F8)
val DividerColor = Color(0xFFE5E5EA)
val PrimaryBlue = Color(0xFF0014CC)
val DangerRed = Color(0xFFFF2D70)

fun NavController.navigateToSettings(navOptions: NavOptions? = null) =
    if (navOptions != null) {
        navigate(route = ScreenRoute.Settings, navOptions)
    } else {
        navigate(ScreenRoute.Settings)
    }

@Composable
fun SettingsScreen(
    innerPadding: PaddingValues,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    var showLogoutDialog by remember { mutableStateOf(false) }
    var notificationsEnabled by remember { mutableStateOf(true) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(innerPadding)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            ProfileCard(
                user = state.user,
                onEditClick = { /* TODO: Handle Edit Profile */ }
            )

            // 2. Account Section
//            SettingsSectionTitle(title = "ACCOUNT")
//
//            SettingsItemRow(
//                title = "Notifications",
//                icon = Icons.Outlined.Notifications,
//                trailing = {
//                    Switch(
//                        checked = notificationsEnabled,
//                        onCheckedChange = { notificationsEnabled = it },
//                        colors = SwitchDefaults.colors(checkedTrackColor = PrimaryBlue)
//                    )
//                }
//            )
//            HorizontalDivider(color = DividerColor)
//            SettingsItemRow(
//                title = "Security & Privacy",
//                icon = Icons.Outlined.Lock,
//                onClick = { /* TODO */ },
//                trailing = { SettingsChevron() }
//            )
//            HorizontalDivider(color = DividerColor)
//            SettingsItemRow(
//                title = "Payment Methods",
//                icon = Icons.Outlined.AccountBalanceWallet,
//                onClick = { /* TODO */ },
//                trailing = { SettingsChevron() }
//            )
//            HorizontalDivider(color = DividerColor)
//
//            // 3. System Section
//            SettingsSectionTitle(title = "SYSTEM")
//
//            SettingsItemRow(
//                title = "Language",
//                icon = Icons.Outlined.Language,
//                onClick = { /* TODO */ },
//                trailing = { Text("English (US)", color = TextSubGrey, fontSize = 14.sp) }
//            )
//            HorizontalDivider(color = DividerColor)
            SettingsItemRow(
                title = "Terms of Service",
                icon = Icons.Outlined.Article,
                onClick = { /* TODO */ },
                trailing = {
                    Icon(
                        imageVector = Icons.Outlined.OpenInNew,
                        contentDescription = "External Link",
                        tint = TextSubGrey,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )
            HorizontalDivider(color = DividerColor)

            // 4. Danger Zone Section
//            SettingsSectionTitle(title = "DANGER ZONE", textColor = DangerRed)

            SettingsItemRow(
                title = "Log Out",
                icon = Icons.AutoMirrored.Outlined.Logout,
                titleColor = DangerRed,
                iconTint = DangerRed,
                onClick = { showLogoutDialog = true }
            )
            HorizontalDivider(color = DividerColor)

            Spacer(modifier = Modifier.height(32.dp))
        }

        if (showLogoutDialog) {
            LogoutConfirmationDialog(
                onConfirm = {
                    showLogoutDialog = false
                    viewModel.logout()
                },
                onDismiss = { showLogoutDialog = false }
            )
        }
    }
}


@Composable
fun SettingsSectionTitle(
    title: String,
    textColor: Color = TextSubGrey,
    modifier: Modifier = Modifier
) {
    Text(
        text = title.uppercase(),
        fontSize = 12.sp,
        fontStyle = MaterialTheme.typography.titleSmall.fontStyle,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        color = textColor,
        modifier = modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 8.dp)
    )
}

@Composable
fun SettingsItemRow(
    title: String,
    icon: ImageVector,
    titleColor: Color = TextMainBlack,
    iconTint: Color = TextMainBlack,
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null, onClick = onClick ?: {})
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = titleColor,
            modifier = Modifier.weight(1f)
        )

        if (trailing != null) {
            trailing()
        }
    }
}

@Composable
fun SettingsChevron() {
    Icon(
        imageVector = Icons.Default.ChevronRight,
        contentDescription = "Navigate",
        tint = TextSubGrey,
        modifier = Modifier.size(20.dp)
    )
}


@Preview(showBackground = true, device = Devices.PIXEL_4)
@Composable
fun SettingsScreenPreview() {
    MaterialTheme {
        SettingsScreen(innerPadding = PaddingValues())
    }
}