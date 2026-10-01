package com.cadena.pataspacasa.presentation.pet.lostmode

import androidx.compose.foundation.layout.*
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
                is LostPetScanDetailUiState.Error -> ErrorView(s.message)
                is LostPetScanDetailUiState.Success -> ScanDetailContent(scan = s.scan)
            }
        }
    }
}

@Composable
private fun ScanDetailContent(scan: LostPetScanEvent) {
    val dateFormat = remember { SimpleDateFormat("d MMM yyyy, HH:mm:ss", Locale("es", "MX")) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("📡", style = MaterialTheme.typography.displaySmall, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)

        SectionCard("Fecha y hora") {
            Text(dateFormat.format(scan.scannedAt), style = MaterialTheme.typography.bodyMedium)
        }

        if (scan.latitude != null && scan.longitude != null) {
            SectionCard("Ubicación aproximada") {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Latitud: ${scan.latitude}", style = MaterialTheme.typography.bodyMedium)
                    Text("Longitud: ${scan.longitude}", style = MaterialTheme.typography.bodyMedium)
                    if (!scan.locationDescription.isNullOrEmpty()) {
                        Text(scan.locationDescription, style = MaterialTheme.typography.bodySmall, color = AppColors.TextSecondary)
                    }
                }
            }
        }
    }
}
