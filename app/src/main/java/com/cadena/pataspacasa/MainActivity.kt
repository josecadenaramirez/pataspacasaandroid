package com.cadena.pataspacasa

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.cadena.pataspacasa.data.service.GooglePlayBillingService
import com.cadena.pataspacasa.presentation.navigation.AppNavHost
import com.cadena.pataspacasa.presentation.navigation.parseLaunchIntent
import com.cadena.pataspacasa.presentation.theme.AppColors
import com.cadena.pataspacasa.presentation.theme.PatasPaCasaTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var billingService: GooglePlayBillingService

    private val launchIntentState = mutableStateOf<com.cadena.pataspacasa.presentation.navigation.LaunchIntent?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        billingService.setActivity(this)
        launchIntentState.value = parseLaunchIntent(intent)

        setContent {
            PatasPaCasaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = AppColors.BackgroundPrimary
                ) {
                    val navController = rememberNavController()
                    AppNavHost(
                        navController = navController,
                        pendingLaunchIntent = launchIntentState.value,
                        onLaunchIntentConsumed = { launchIntentState.value = null }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        launchIntentState.value = parseLaunchIntent(intent)
    }
}
