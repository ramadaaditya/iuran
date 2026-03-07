package com.ramstudio.kaskita.presentation.settings

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.ramstudio.kaskita.R
import com.ramstudio.kaskita.core.navigation.ScreenRoute
import com.ramstudio.kaskita.presentation.settings.component.DeleteAccountConfirmationDialog
import com.ramstudio.kaskita.presentation.settings.component.LogoutConfirmationDialog
import com.ramstudio.kaskita.presentation.settings.component.ProfileCard

val TextMainBlack = Color(0xFF15171A)
val TextSubGrey = Color(0xFF69707A)
val BackgroundSoft = Color(0xFFF3F5F8)
val CardBorder = Color(0xFFE4E8EF)
val PrimaryBlue = Color(0xFF0A49D1)
val DangerRed = Color(0xFFD92D20)

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
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var deleteRequestInFlight by remember { mutableStateOf(false) }

    LaunchedEffect(state.error) {
        state.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(state.isDeletingAccount, state.error, deleteRequestInFlight) {
        if (deleteRequestInFlight && !state.isDeletingAccount && state.error == null) {
            showDeleteAccountDialog = false
            deleteRequestInFlight = false
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(innerPadding)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            ProfileCard(
                user = state.user,
                onEditClick = { }
            )

            SettingsSectionCard(title = stringResource(R.string.settings_section_support)) {
                SettingsItemRow(
                    title = stringResource(R.string.settings_terms_title),
                    subtitle = stringResource(R.string.settings_terms_subtitle),
                    icon = Icons.AutoMirrored.Outlined.Article,
                    onClick = { },
                    trailing = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                            contentDescription = stringResource(R.string.settings_open_terms_cd),
                            tint = TextSubGrey,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
            }

            SettingsSectionCard(
                title = stringResource(R.string.settings_section_danger),
                titleColor = DangerRed
            ) {
                SettingsItemRow(
                    title = stringResource(R.string.settings_delete_account_title),
                    subtitle = stringResource(R.string.settings_delete_account_subtitle),
                    icon = Icons.Outlined.DeleteForever,
                    titleColor = DangerRed,
                    iconTint = DangerRed,
                    onClick = {
                        if (!state.isDeletingAccount) {
                            showDeleteAccountDialog = true
                        }
                    },
                    trailing = {
                        if (state.isDeletingAccount) {
                            CircularProgressIndicator(
                                strokeWidth = 2.dp,
                                color = DangerRed,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                )

                HorizontalDivider(color = CardBorder)

                SettingsItemRow(
                    title = stringResource(R.string.settings_logout_title),
                    subtitle = stringResource(R.string.settings_logout_subtitle),
                    icon = Icons.AutoMirrored.Outlined.Logout,
                    titleColor = DangerRed,
                    iconTint = DangerRed,
                    onClick = { showLogoutDialog = true }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
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

        if (showDeleteAccountDialog) {
            DeleteAccountConfirmationDialog(
                isLoading = state.isDeletingAccount,
                onConfirm = {
                    deleteRequestInFlight = true
                    viewModel.deleteAccount()
                },
                onDismiss = {
                    if (!state.isDeletingAccount) {
                        showDeleteAccountDialog = false
                        deleteRequestInFlight = false
                    }
                }
            )
        }
    }
}


@Composable
private fun SettingsSectionCard(
    title: String,
    titleColor: Color = TextSubGrey,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title.uppercase(),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = titleColor,
            letterSpacing = 0.8.sp,
            modifier = Modifier.padding(start = 4.dp)
        )

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(content = content)
        }
    }
}

@Composable
private fun SettingsItemRow(
    title: String,
    subtitle: String,
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
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(color = BackgroundSoft, shape = RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = titleColor,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextSubGrey
            )
        }

        trailing?.invoke()
    }
}

@Preview(showBackground = true, device = Devices.PIXEL_4)
@Composable
fun SettingsScreenPreview() {
    MaterialTheme {
        SettingsScreen(innerPadding = PaddingValues())
    }
}
