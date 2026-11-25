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
import com.cadena.pataspacasa.presentation.common.*
import com.cadena.pataspacasa.presentation.theme.AppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LostModeScreen(
    petId: String,
    viewModel: LostModeViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val step by viewModel.step.collectAsState()
    val showConfirmActivation by viewModel.showConfirmActivation.collectAsState()
    val showConfirmDeactivation by viewModel.showConfirmDeactivation.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    LaunchedEffect(petId) { viewModel.setPetId(petId) }

    errorMessage?.let { message ->
        AlertDialog(
            onDismissRequest = viewModel::clearError,
            title = { Text("No se pudo actualizar") },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = viewModel::clearError) { Text("Entendido") }
            }
        )
    }

    if (showConfirmActivation) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissActivation() },
            title = { Text("¿Activar Modo Perdido para ${viewModel.petName}?") },
            text = { Text("Cuando alguien escanee la placa de ${viewModel.petName}, verá de inmediato que está perdida y cómo contactarte.") },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmActivation() }) {
                    Text("Activar Modo Perdido", color = AppColors.Error)
                }
            },
            dismissButton = { TextButton(onClick = { viewModel.dismissActivation() }) { Text("Cancelar") } }
        )
    }

    if (showConfirmDeactivation) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissDeactivation() },
            title = { Text("¿Desactivar Modo Perdido?") },
            text = { Text("La placa de ${viewModel.petName} dejará de mostrar la alerta de mascota perdida.") },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmDeactivation() }) {
                    Text("Desactivar", color = AppColors.Error)
                }
            },
            dismissButton = { TextButton(onClick = { viewModel.dismissDeactivation() }) { Text("Cancelar") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Modo Perdido") },
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
            when (val s = step) {
                is LostModeStep.Loading -> LoadingView("Cargando…")
                is LostModeStep.Processing -> LoadingView("Aplicando cambios…")
                is LostModeStep.Error -> ErrorView(s.message, onRetry = { viewModel.retryAfterError() })

                is LostModeStep.Inactive -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        Spacer(Modifier.height(32.dp))

                        Text("🚨", style = MaterialTheme.typography.displaySmall)

                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Modo Perdido", style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
                            Text(
                                "Activa esta función para que cualquier persona que escanee la placa de ${s.pet.name} sepa inmediatamente que está perdida y cómo contactarte.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = AppColors.TextSecondary,
                                textAlign = TextAlign.Center
                            )
                        }

                        SectionCard("¿Qué pasa al activar?") {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                FeatureRow("👁️", AppColors.Coral, "Cualquier persona verá la alerta al escanear la placa.")
                                FeatureRow("🔔", AppColors.Primary, "Recibirás una notificación con cada escaneo.")
                                FeatureRow("📍", AppColors.Mustard, "Se registra la hora y ubicación de cada escaneo.")
                            }
                        }

                        DestructiveButton(
                            title = "Activar Modo Perdido",
                            onClick = { viewModel.requestActivation() }
                        )

                        Spacer(Modifier.height(32.dp))
                    }
                }

                is LostModeStep.Active -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        Spacer(Modifier.height(32.dp))

                        Text("🚨", style = MaterialTheme.typography.displaySmall)

                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Modo Perdido activo", style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
                            Text(
                                "La placa de ${s.pet.name} muestra la alerta de perdido al ser escaneada.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = AppColors.TextSecondary,
                                textAlign = TextAlign.Center
                            )
                        }

                        Card(
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = AppColors.Success.copy(alpha = 0.12f))
                        ) {
                            Text(
                                "✓ Activo — La gente puede ver que ${s.pet.name} está perdida.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = AppColors.Success,
                                modifier = Modifier.padding(16.dp),
                                textAlign = TextAlign.Center
                            )
                        }

                        OutlinedButton(
                            onClick = { viewModel.requestDeactivation() },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.Error)
                        ) {
                            Text("Desactivar Modo Perdido")
                        }

                        Spacer(Modifier.height(32.dp))
                    }
                }

                is LostModeStep.ActivationSuccess -> {
                    SuccessContent(
                        title = "Modo Perdido activado",
                        description = "La placa de ${s.pet.name} ahora muestra la alerta de mascota perdida.",
                        onDone = onNavigateBack
                    )
                }

                is LostModeStep.DeactivationSuccess -> {
                    SuccessContent(
                        title = "Modo Perdido desactivado",
                        description = "La placa de ${s.pet.name} ya no muestra la alerta.",
                        onDone = onNavigateBack
                    )
                }
            }
        }
    }
}

@Composable
private fun FeatureRow(emoji: String, color: androidx.compose.ui.graphics.Color, text: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
        Text(emoji)
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium,
            color = AppColors.TextSecondary,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SuccessContent(title: String, description: String, onDone: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(Modifier.height(48.dp))
        Text("✅", style = MaterialTheme.typography.displaySmall)
        Text(title, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
        Text(description, style = MaterialTheme.typography.bodyMedium, color = AppColors.TextSecondary, textAlign = TextAlign.Center)
        Spacer(Modifier.height(16.dp))
        PrimaryButton("Listo", onClick = onDone)
    }
}
