package com.cadena.pataspacasa.presentation.pet.lostmode

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cadena.pataspacasa.domain.entity.LostPetScanEvent
import com.cadena.pataspacasa.presentation.common.*
import com.cadena.pataspacasa.presentation.theme.AppColors
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LostPetScanHistoryScreen(
    petId: String,
    petName: String,
    viewModel: LostPetScanHistoryViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToScanDetail: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(petId) { viewModel.setPetId(petId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial — $petName") },
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
        when (val state = uiState) {
            is LostPetScanHistoryUiState.Loading -> LoadingView("Cargando historial…")
            is LostPetScanHistoryUiState.Error -> ErrorView(state.message, onRetry = { viewModel.load() })
            is LostPetScanHistoryUiState.Empty -> EmptyStateView(
                title = "Sin escaneos",
                description = "No se han registrado escaneos de la placa aún."
            )
            is LostPetScanHistoryUiState.Success -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.scans, key = { it.id }) { scan ->
                        ScanCard(scan = scan, onClick = { onNavigateToScanDetail(scan.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun ScanCard(scan: LostPetScanEvent, onClick: () -> Unit) {
    val dateFormat = remember { SimpleDateFormat("d MMM yyyy, HH:mm", Locale("es", "MX")) }
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.BackgroundSurface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(dateFormat.format(scan.scannedAt), style = MaterialTheme.typography.titleMedium)
            if (!scan.locationDescription.isNullOrEmpty()) {
                Text(scan.locationDescription, style = MaterialTheme.typography.bodySmall, color = AppColors.TextSecondary)
            }
            if (scan.latitude != null && scan.longitude != null) {
                Text(
                    "📍 ${String.format("%.4f", scan.latitude)}, ${String.format("%.4f", scan.longitude)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.TextSecondary
                )
            }
        }
    }
}
