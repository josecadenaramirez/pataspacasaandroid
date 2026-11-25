package com.cadena.pataspacasa.presentation.pet.lostmode

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cadena.pataspacasa.domain.entity.LostPetScanEvent
import com.cadena.pataspacasa.presentation.common.*
import com.cadena.pataspacasa.presentation.theme.AppColors
import java.text.SimpleDateFormat
import java.util.*
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LostPetScanDetailScreen(
    eventId: String,
    petId: String,
    viewModel: LostPetScanDetailViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(eventId, petId) { viewModel.load(eventId, petId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle del escaneo") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppColors.BackgroundPrimary)
            )
        },
        containerColor = AppColors.BackgroundPrimary
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when (val s = uiState) {
                is LostPetScanDetailUiState.Loading -> LoadingView("Cargando…")
                is LostPetScanDetailUiState.Error -> ErrorView(
                    s.message,
                    onRetry = { viewModel.load(eventId, petId) }
                )
                is LostPetScanDetailUiState.Success -> ScanDetailContent(scan = s.scan)
            }
        }
    }
}

@Composable
private fun ScanDetailContent(scan: LostPetScanEvent) {
    val dateFormat = remember { SimpleDateFormat("d MMM yyyy, HH:mm:ss", Locale("es", "MX")) }
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("📡", style = MaterialTheme.typography.displaySmall, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)

        SectionCard("Fecha y hora") {
            Text(scan.scannedAt?.let(dateFormat::format) ?: "No disponible", style = MaterialTheme.typography.bodyMedium)
        }

        if (scan.visitorDeniedLocation) {
            SectionCard("Ubicación") {
                Text("La persona que escaneó decidió no compartir su ubicación.", color = AppColors.TextSecondary)
            }
        } else if (scan.hasLocation && scan.latitude != null && scan.longitude != null) {
            SectionCard("Ubicación aproximada") {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Latitud: ${scan.latitude}", style = MaterialTheme.typography.bodyMedium)
                    Text("Longitud: ${scan.longitude}", style = MaterialTheme.typography.bodyMedium)
                    if (!scan.locationDescription.isNullOrEmpty()) {
                        Text(scan.locationDescription, style = MaterialTheme.typography.bodySmall, color = AppColors.TextSecondary)
                    }
                    scan.accuracyMeters?.let {
                        Text("Precisión aproximada: ~${it.toInt()} metros", style = MaterialTheme.typography.bodySmall)
                    }
                    Spacer(Modifier.height(8.dp))
                    PrimaryButton(title = "Ver en Maps", onClick = {
                        val uri = Uri.parse("geo:${scan.latitude},${scan.longitude}?q=${scan.latitude},${scan.longitude}")
                        val intent = Intent(Intent.ACTION_VIEW, uri)
                        if (intent.resolveActivity(context.packageManager) != null) context.startActivity(intent)
                    })
                }
            }
        } else {
            SectionCard("Ubicación") {
                Text("No se compartió información de ubicación.", color = AppColors.TextSecondary)
            }
        }
    }
}
