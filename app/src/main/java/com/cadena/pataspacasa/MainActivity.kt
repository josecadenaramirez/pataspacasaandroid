package com.cadena.pataspacasa

import android.os.Bundle
import android.os.Build
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.cadena.pataspacasa.presentation.navigation.AppNavHost
import com.cadena.pataspacasa.presentation.navigation.parseLaunchIntent
import com.cadena.pataspacasa.presentation.theme.AppColors
import com.cadena.pataspacasa.presentation.theme.PatasPaCasaTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import androidx.lifecycle.lifecycleScope
import com.cadena.pataspacasa.domain.repository.AuthRepository
import com.cadena.pataspacasa.domain.repository.SessionState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var authRepository: AuthRepository

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    private val launchIntentState = mutableStateOf<com.cadena.pataspacasa.presentation.navigation.LaunchIntent?>(null)
    private var nfcResult: ((Result<String>) -> Unit)? = null

    fun scanNfc(onResult: (Result<String>) -> Unit) {
        val adapter = NfcAdapter.getDefaultAdapter(this)
        if (adapter == null || !adapter.isEnabled) {
            onResult(Result.failure(IllegalStateException("NFC no disponible")))
            return
        }
        nfcResult = onResult
        adapter.enableReaderMode(
            this,
            { tag -> handleNfcTag(tag) },
            NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_NFC_B or
                NfcAdapter.FLAG_READER_NFC_F or NfcAdapter.FLAG_READER_NFC_V,
            null
        )
    }

    fun cancelNfcScan() {
        NfcAdapter.getDefaultAdapter(this)?.disableReaderMode(this)
        nfcResult = null
    }

    private fun handleNfcTag(tag: Tag) {
        val result = runCatching {
            val ndef = Ndef.get(tag) ?: error("La placa no contiene un enlace NDEF")
            ndef.connect()
            val record = try { ndef.ndefMessage?.records?.firstOrNull() } finally { ndef.close() }
                ?: error("La placa está vacía")
            record.toUri()?.toString()?.takeIf { it.isNotBlank() }
                ?: decodeTextRecord(record.payload)
        }
        runOnUiThread {
            NfcAdapter.getDefaultAdapter(this)?.disableReaderMode(this)
            nfcResult?.invoke(result)
            nfcResult = null
        }
    }

    private fun decodeTextRecord(payload: ByteArray): String {
        if (payload.isEmpty()) error("Contenido NFC inválido")
        val languageLength = payload[0].toInt() and 0x3F
        val start = 1 + languageLength
        if (start >= payload.size) error("Contenido NFC inválido")
        return payload.copyOfRange(start, payload.size).toString(Charsets.UTF_8)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )
        launchIntentState.value = parseLaunchIntent(intent)
        observeNotificationPermission()

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

    private fun observeNotificationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        lifecycleScope.launch {
            authRepository.observeAuthState().collectLatest { state ->
                if (state !is SessionState.Authenticated) return@collectLatest
                val prefs = getSharedPreferences("pataspa_permissions", MODE_PRIVATE)
                val alreadyRequested = prefs.getBoolean("notifications_requested", false)
                if (!alreadyRequested && ContextCompat.checkSelfPermission(
                        this@MainActivity,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    prefs.edit().putBoolean("notifications_requested", true).apply()
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        launchIntentState.value = parseLaunchIntent(intent)
    }

    override fun onPause() {
        cancelNfcScan()
        super.onPause()
    }
}
