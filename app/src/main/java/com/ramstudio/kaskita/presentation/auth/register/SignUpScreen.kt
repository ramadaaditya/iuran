package com.ramstudio.kaskita.presentation.auth.register

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Devices.PIXEL_5
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ramstudio.kaskita.R
import com.ramstudio.kaskita.ui.theme.KasKitaTheme

// --- Tema Warna Selaras ---
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
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel.uiEvent) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is SignUpUiEvent.NavigateSignIn -> onNavigateSignIn()
                is SignUpUiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = BgColor // Set background Scaffold
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

            Column(horizontalAlignment = Alignment.Start) {
                Text(
                    text = "Full Name",
                    color = TextDark,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = uiState.fullName,
                    onValueChange = onFullNameChange,
                    placeholder = {
                        Text(
                            text = "Ramada Aditya",
                            color = TextGrey.copy(alpha = 0.7f)
                        )
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = PrimaryGreen,
                        unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f),
                        focusedTextColor = TextDark,
                        unfocusedTextColor = TextDark,
                        cursorColor = PrimaryGreen
                    )
                )
            }
            Spacer(Modifier.height(20.dp))

            Column(horizontalAlignment = Alignment.Start) {
                Text(
                    text = "Email",
                    color = TextDark,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = uiState.email,
                    onValueChange = onEmailChange,
                    placeholder = {
                        Text(
                            text = "john.doe@example.com",
                            color = TextGrey.copy(alpha = 0.7f)
                        )
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = PrimaryGreen,
                        unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f),
                        focusedTextColor = TextDark,
                        unfocusedTextColor = TextDark,
                        cursorColor = PrimaryGreen
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Column(horizontalAlignment = Alignment.Start) {
                Text(
                    text = "Password",
                    color = TextDark,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = uiState.password,
                    onValueChange = onPasswordChange,
                    placeholder = {
                        Text(
                            text = "Enter your password",
                            color = TextGrey.copy(alpha = 0.7f)
                        )
                    },
                    visualTransformation = PasswordVisualTransformation(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = PrimaryGreen,
                        unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f),
                        focusedTextColor = TextDark,
                        unfocusedTextColor = TextDark,
                        cursorColor = PrimaryGreen
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onSignUpClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryGreen,
                    disabledContainerColor = PrimaryGreen.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Sign up",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tambahan: Tombol Google Sign In yang sudah disesuaikan
            // Uncomment dan gunakan jika diperlukan
            /*
            GoogleSignInButton(onClick = { /* TODO: Handle Google Sign In */ })
            Spacer(modifier = Modifier.height(16.dp))
            */

            TextButton(onClick = navigateSignIn) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(
                            style = SpanStyle(
                                fontWeight = FontWeight.Normal,
                                color = TextGrey
                            )
                        ) {
                            append("Already have an account? ")
                        }

                        withStyle(
                            style = SpanStyle(
                                fontWeight = FontWeight.Bold,
                                color = PrimaryGreen
                            )
                        ) {
                            append("Log in")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun RegisterHeader() {
    Text(
        text = "Create Account",
        style = MaterialTheme.typography.headlineLarge,
        color = TextDark,
        fontWeight = FontWeight.ExtraBold,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = "Join your community and start tracking you cash flow with ease",
        style = MaterialTheme.typography.bodyMedium,
        color = TextGrey,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
fun GoogleSignInButton(onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = TextDark
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        // Pastikan Anda memiliki icon Google (ic_google) di res/drawable
        Image(
            painter = painterResource(R.drawable.ic_google),
            contentDescription = "Google Sign In",
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = "Sign in with Google",
            fontWeight = FontWeight.Medium
        )
    }
}

// Gradient baru untuk tema terang
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
                        BgColor // Membaur dengan background utama
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