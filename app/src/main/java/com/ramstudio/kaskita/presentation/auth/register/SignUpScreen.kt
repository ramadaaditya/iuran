package com.ramstudio.kaskita.presentation.auth.register

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
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
import com.ramstudio.kaskita.core.ui.theme.KasKitaTheme
import com.ramstudio.kaskita.core.utils.LocalAppSnackbarHostState

val BgColor = Color(0xFFFBFCFD)
val PrimaryGreen = Color(0xFF00BFA5)
val TextDark = Color(0xFF1A1A1A)
val TextGrey = Color(0xFF757575)
val LightGreenBg = Color(0xFFE0F7FA)

@Composable
fun SignUpScreen(
    onNavigateSignIn: () -> Unit,
    viewModel: SignUpViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = LocalAppSnackbarHostState.current

    LaunchedEffect(viewModel.uiEvent) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is SignUpUiEvent.NavigateSignIn -> onNavigateSignIn()
                is SignUpUiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        containerColor = BgColor
    ) { paddingValues ->
        SignUpContent(
            modifier = Modifier.padding(paddingValues),
            uiState = uiState,
            onFullNameChange = { viewModel.onFullNameChange(it) },
            onEmailChange = { viewModel.onEmailChange(it) },
            onPasswordChange = { viewModel.onPasswordChange(it) },
            onSignUpClick = { viewModel.signUpWithEmail() },
            navigateSignIn = onNavigateSignIn
        )
    }
}

@Composable
fun SignUpContent(
    modifier: Modifier = Modifier,
    uiState: SignUpUiState,
    onFullNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSignUpClick: () -> Unit,
    navigateSignIn: () -> Unit
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
                .padding(top = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            RegisterHeader()
            Spacer(modifier = Modifier.height(32.dp))
            KasKitaTextField(
                label = stringResource(R.string.signup_fullname_label),
                value = uiState.fullName,
                onValueChange = onFullNameChange,
                placeholder = stringResource(R.string.signup_fullname_placeholder),
                errorMessage = uiState.fullNameError
            )

            Spacer(Modifier.height(12.dp))

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
                label = stringResource(R.string.signup_password_label),
                placeholder = stringResource(R.string.signin_password_placeholder),
                errorMessage = uiState.passwordError

            )

            Spacer(modifier = Modifier.height(24.dp))

            KasKitaButton(
                onClick = onSignUpClick,
                enabled = !uiState.isLoading &&
                        uiState.email.isNotBlank() &&
                        uiState.password.isNotBlank() &&
                        uiState.fullName.isNotBlank(),
                isLoading = uiState.isLoading,
                label = stringResource(R.string.signup_button)
            )

            Spacer(modifier = Modifier.height(12.dp))

            SignInText(navigateSignIn = navigateSignIn)
        }
    }
}


@Composable
fun SignInText(
    navigateSignIn: () -> Unit
) {
    val annotatedText = buildAnnotatedString {
        append(stringResource(R.string.signup_have_account_prefix))
        append(" ")

        pushStringAnnotation(
            tag = "SIGN_IN",
            annotation = "sign_in"
        )

        withStyle(
            style = SpanStyle(
                fontWeight = FontWeight.Bold,
                color = PrimaryGreen
            )
        ) {
            append(stringResource(R.string.signup_have_account_action))
        }

        pop()
    }

    ClickableText(
        text = annotatedText,
        onClick = { offset ->
            annotatedText.getStringAnnotations(
                tag = "SIGN_IN",
                start = offset,
                end = offset
            ).firstOrNull()?.let {
                navigateSignIn()
            }
        }
    )
}

@Composable
fun RegisterHeader() {
    Text(
        text = stringResource(R.string.signup_header_title),
        style = MaterialTheme.typography.headlineLarge,
        color = TextDark,
        fontWeight = FontWeight.ExtraBold,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = stringResource(R.string.signup_header_subtitle),
        style = MaterialTheme.typography.bodyMedium,
        color = TextGrey,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
fun LightGradient() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.35f)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        LightGreenBg.copy(alpha = 0.6f),
                        BgColor
                    )
                )
            )
    )
}

@Preview(showBackground = true, device = PIXEL_5)
@Composable
private fun SignUpScreenPreview() {
    KasKitaTheme {
        SignUpContent(
            uiState = SignUpUiState(
                email = "ramadaaditya100@gmail.com",
                fullName = "Ramada Aditya",
                password = "Password123",
            ),
            onEmailChange = {},
            onPasswordChange = {},
            onSignUpClick = {},
            onFullNameChange = {},
            navigateSignIn = {}
        )
    }
}
