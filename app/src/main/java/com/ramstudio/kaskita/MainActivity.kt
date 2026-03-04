package com.ramstudio.kaskita

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ramstudio.kaskita.ui.KasKitaApp
import com.ramstudio.kaskita.ui.rememberKaskitaState
import com.ramstudio.kaskita.ui.theme.KasKitaTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().setKeepOnScreenCondition {
            mainViewModel.sessionStatus.value is AuthState.Loading
        }
        super.onCreate(savedInstanceState)
        val appPreferences = getSharedPreferences("kaskita_prefs", MODE_PRIVATE)
        val onboardingCompleted = appPreferences.getBoolean("onboarding_completed", false)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                Color.TRANSPARENT,
                Color.TRANSPARENT,
                // Beri tahu sistem: "Jika background saya terang, gunakan ikon gelap"
            ),
            navigationBarStyle = SystemBarStyle.auto(
                Color.TRANSPARENT,
                Color.TRANSPARENT,
            )
        )
        setContent {
            KasKitaTheme {
                val authState by mainViewModel.sessionStatus.collectAsStateWithLifecycle()
                var showOnboarding by rememberSaveable { mutableStateOf(!onboardingCompleted) }
                val appState = rememberKaskitaState()
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    KasKitaApp(
                        appState = appState,
                        authState = authState,
                        showOnboarding = showOnboarding,
                        onOnboardingFinished = {
                            appPreferences.edit()
                                .putBoolean("onboarding_completed", true)
                                .apply()
                            showOnboarding = false
                        }
                    )
                }
            }
        }
    }
}
