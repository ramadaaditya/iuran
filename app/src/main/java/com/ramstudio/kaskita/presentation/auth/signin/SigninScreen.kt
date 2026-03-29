package com.ramstudio.kaskita.presentation.auth.signin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Devices.PIXEL_5
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ramstudio.kaskita.R
import com.ramstudio.kaskita.core.ui.component.KasKitaButton
import com.ramstudio.kaskita.core.ui.component.KasKitaTextField
import com.ramstudio.kaskita.core.ui.component.PasswordField
import com.ramstudio.kaskita.presentation.auth.register.LightGradient
import com.ramstudio.kaskita.presentation.auth.register.PrimaryGreen

val BgColor = Color(0xFFFBFCFD)
val PrimaryGreen = Color(0xFF00BFA5)
val TextDark = Color(0xFF1A1A1A)
val TextGrey = Color(0xFF757575)

@Composable
fun SignInScreen(
    onNavigateSignUp: () -> Unit,
    viewModel: SignInViewModel = hiltViewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel.uiEvent) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is SignInUiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
                is SignInUiEvent.NavigateHome -> {}
                is SignInUiEvent.NavigateSignUp -> onNavigateSignUp()
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = BgColor
    ) { paddingValues ->
        SignInContent(
            uiState = uiState,
            onEmailChange = { viewModel.onEmailChange(it) },
            onPasswordChange = { viewModel.onPasswordChange(it) },
            onSignInClick = { viewModel.signInWithEmail() },
            navigateSignUp = onNavigateSignUp,
            modifier = Modifier.padding(paddingValues)
        )
    }
}

@Composable
fun SignInContent(
    uiState: SignInUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSignInClick: () -> Unit,
    navigateSignUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BgColor),
        contentAlignment = Alignment.TopCenter
    ) {
        LightGradient()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(top = 100.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SignInHeader()

            Spacer(modifier = Modifier.height(32.dp))
            KasKitaTextField(
                label = stringResource(R.string.signin_email_label),
                value = uiState.email,
                onValueChange = onEmailChange,
                placeholder = stringResource(R.string.signin_email_placeholder),
                errorMessage = uiState.emailError
            )

            Spacer(modifier = Modifier.height(12.dp))
            PasswordField(
                value = uiState.password,
                onValueChange = onPasswordChange,
                label = stringResource(R.string.signin_password_label),
                placeholder = stringResource(R.string.signin_password_placeholder),
                errorMessage = uiState.passwordError
            )

            Spacer(modifier = Modifier.height(24.dp))

            KasKitaButton(
                onClick = onSignInClick,
                enabled = !uiState.isLoading &&
                        uiState.email.isNotBlank() &&
                        uiState.password.isNotBlank(),
                isLoading = uiState.isLoading,
                label = stringResource(R.string.signin_button)
            )

            Spacer(modifier = Modifier.height(12.dp))
            SignUpText(navigateSignUp = navigateSignUp)

        }
    }
}


@Composable
fun SignUpText(
    navigateSignUp: () -> Unit
) {
    val annotatedText = buildAnnotatedString {
        append(stringResource(R.string.signin_no_account_prefix))

        append(" ")

        pushStringAnnotation(
            tag = "SIGN_UP",
            annotation = "sign_up"
        )

        withStyle(
            style = SpanStyle(
                fontWeight = FontWeight.Bold,
                color = PrimaryGreen
            )
        ) {
            append(stringResource(R.string.signin_no_account_action))
        }

        pop()
    }

    ClickableText(
        text = annotatedText,
        onClick = { offset ->
            annotatedText.getStringAnnotations(
                tag = "SIGN_UP",
                start = offset,
                end = offset
            ).firstOrNull()?.let {
                navigateSignUp()
            }
        }
    )
}

@Composable
fun SignInHeader() {
    Text(
        text = stringResource(R.string.signin_header_title),
        style = MaterialTheme.typography.headlineLarge,
        color = TextDark,
        fontWeight = FontWeight.ExtraBold
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = stringResource(R.string.signin_header_subtitle),
        style = MaterialTheme.typography.bodyMedium,
        color = TextGrey,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.fillMaxWidth()
    )
}

@Preview(showBackground = true, device = PIXEL_5)
@Composable
private fun SignInScreenPreview() {
    MaterialTheme {
        SignInContent(
            uiState = SignInUiState(
                email = "ramada@example.com",
                password = "password123"
            ),
            onEmailChange = {},
            onPasswordChange = {},
            onSignInClick = {},
            navigateSignUp = {}
        )
    }
}
