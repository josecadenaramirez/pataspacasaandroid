package com.cadena.pataspacasa.presentation.pet

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.cadena.pataspacasa.domain.entity.PetSpecies
import com.cadena.pataspacasa.presentation.common.*
import com.cadena.pataspacasa.presentation.theme.AppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetDetailScreen(
    petId: String,
    viewModel: PetDetailViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToEditPet: (String) -> Unit,
    onNavigateToLostMode: (String) -> Unit,
    onNavigateToLinkTag: (String) -> Unit,
    onNavigateToScanHistory: (String, String) -> Unit,
    onNavigateToPremium: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val isChangingLostMode by viewModel.isChangingLostMode.collectAsState()
    val showPremiumUpsell by viewModel.showPremiumUpsell.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    var showLostModeConfirm by remember { mutableStateOf(false) }
    var showDisableLostModeConfirm by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            val bytes = context.contentResolver.openInputStream(it)?.use { stream -> stream.readBytes() }
            bytes?.let { data -> viewModel.updatePhoto(data) }
        }
    }

    LaunchedEffect(petId) { viewModel.setPetId(petId) }

    errorMessage?.let { msg ->
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = { Text("Error") },
            text = { Text(msg) },
            confirmButton = { TextButton(onClick = { viewModel.clearError() }) { Text("OK") } }
        )
    }

    if (showPremiumUpsell) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissPremiumUpsell() },
            title = { Text("Función Premium") },
            text = { Text("Esta función está disponible solo en el plan Premium. ¿Deseas ver los planes?") },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissPremiumUpsell(); onNavigateToPremium() }) { Text("Ver Premium") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissPremiumUpsell() }) { Text("Cancelar") }
            }
        )
    }

    val pet = (uiState as? PetDetailUiState.Success)?.pet

    if (showLostModeConfirm && pet != null) {
        AlertDialog(
            onDismissRequest = { showLostModeConfirm = false },
            title = { Text("¿Activar Modo Perdido para ${pet.name}?") },
            text = { Text("Cuando alguien escanee la placa de ${pet.name}, verá de inmediato que está perdida y cómo contactarte.") },
            confirmButton = {
                TextButton(onClick = { showLostModeConfirm = false; viewModel.enableLostMode() }) {
                    Text("Activar", color = AppColors.Error)
                }
            },
            dismissButton = { TextButton(onClick = { showLostModeConfirm = false }) { Text("Cancelar") } }
        )
    }

    if (showDisableLostModeConfirm && pet != null) {
        AlertDialog(
            onDismissRequest = { showDisableLostModeConfirm = false },
            title = { Text("¿Desactivar Modo Perdido?") },
            text = { Text("La placa de ${pet.name} dejará de mostrar la alerta de mascota perdida.") },
            confirmButton = {
                TextButton(onClick = { showDisableLostModeConfirm = false; viewModel.disableLostMode() }) {
                    Text("Desactivar", color = AppColors.Error)
                }
            },
            dismissButton = { TextButton(onClick = { showDisableLostModeConfirm = false }) { Text("Cancelar") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(pet?.name ?: "Detalle") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                },
                actions = {
                    if (pet?.canEditProfile == true) {
                        IconButton(onClick = { pet.id.let(onNavigateToEditPet) }) {
                            Icon(Icons.Default.Edit, "Editar")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppColors.BackgroundPrimary)
            )
        },
        containerColor = AppColors.BackgroundPrimary
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when (val state = uiState) {
                is PetDetailUiState.Loading -> LoadingView("Cargando mascota…")
                is PetDetailUiState.NotFound -> EmptyStateView(
                    title = "Mascota no encontrada",
                    description = "No pudimos encontrar la información de esta mascota."
                )
                is PetDetailUiState.Error -> ErrorView(state.message, onRetry = { viewModel.loadPet() })
                is PetDetailUiState.Success -> {
                    val p = state.pet
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Lost banner
                        if (p.isLost && p.isPremiumActive) {
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = AppColors.Coral.copy(alpha = 0.12f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text("🚨", style = MaterialTheme.typography.titleLarge)
                                    Column {
                                        Text(
                                            "Modo Perdido activo",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = AppColors.Coral
                                        )
                                        Text(
                                            "La placa de ${p.name} muestra la alerta de perdido.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = AppColors.TextSecondary
                                        )
                                    }
                                }
                            }
                        }

                        // Hero
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(CircleShape)
                                    .background(AppColors.BackgroundSecondary)
                                    .then(
                                        if (p.canUpdatePhoto)
                                            Modifier.clickable { imagePickerLauncher.launch("image/*") }
                                        else Modifier
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (!p.photoUrl.isNullOrEmpty()) {
                                    AsyncImage(
                                        model = p.photoUrl,
                                        contentDescription = p.name,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Text(
                                        text = when (p.species) {
                                            PetSpecies.DOG -> "🐕"
                                            PetSpecies.CAT -> "🐈"
                                            PetSpecies.OTHER -> "🐾"
                                        },
                                        style = MaterialTheme.typography.displaySmall
                                    )
                                }
                                if (p.canUpdatePhoto) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(AppColors.Primary),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.CameraAlt, null, tint = AppColors.TextOnDark, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(p.name, style = MaterialTheme.typography.headlineMedium)
                            Text("${p.breed.ifEmpty { "Raza desconocida" }} · ${p.species.displayName}", style = MaterialTheme.typography.bodyMedium, color = AppColors.TextSecondary)
                            if (p.isPremiumActive) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = AppColors.BadgePremium.copy(alpha = 0.15f),
                                    modifier = Modifier.padding(top = 4.dp)
                                ) {
                                    Text(
                                        "⭐ Premium",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = AppColors.Mustard,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        // Info section
                        SectionCard("Información") {
                            InfoRow("Especie", p.species.displayName)
                            if (p.breed.isNotEmpty()) InfoRow("Raza", p.breed)
                            if (p.age != null) InfoRow("Edad", "${p.age} año(s)")
                            if (p.ownerName?.isNotEmpty() == true) InfoRow("Dueño", p.ownerName)
                        }

                        // Contact phones
                        if (p.phones.isNotEmpty()) {
                            SectionCard("Teléfonos de contacto") {
                                p.phones.forEachIndexed { index, phone ->
                                    InfoRow(
                                        label = if (index == 0) "Principal" else "Adicional ${index}",
                                        value = phone
                                    )
                                }
                            }
                        }

                        // Medical section
                        if (!p.allergies.isNullOrEmpty() || !p.treatment.isNullOrEmpty()) {
                            SectionCard("Salud") {
                                if (!p.allergies.isNullOrEmpty()) InfoRow("Alergias", p.allergies)
                                if (!p.treatment.isNullOrEmpty()) InfoRow("Tratamiento", p.treatment)
                            }
                        }

                        // NFC section
                        SectionCard("Placa NFC") {
                            if (p.hasNFCTag) {
                                InfoRow("Código", p.tagId ?: "")
                                InfoRow("Estado", "Vinculada ✓")
                            } else {
                                Text(
                                    "Sin placa NFC vinculada",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = AppColors.TextSecondary
                                )
                                Spacer(Modifier.height(8.dp))
                                OutlinedButton(
                                    onClick = { onNavigateToLinkTag("") },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Vincular placa NFC")
                                }
                            }
                        }

                        // Scan history button (Premium)
                        if (p.isPremiumActive && p.hasNFCTag) {
                            OutlinedButton(
                                onClick = { onNavigateToScanHistory(p.id, p.name) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.History, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Historial de escaneos")
                            }
                        }

                        // Lost mode section
                        SectionCard("Modo Perdido") {
                            if (p.isPremiumActive) {
                                if (p.isLost) {
                                    Text(
                                        "Modo Perdido activo. Tu mascota aparecerá como perdida en escaneos.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = AppColors.TextSecondary
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    DestructiveButton(
                                        title = "Desactivar Modo Perdido",
                                        onClick = { showDisableLostModeConfirm = true },
                                        isLoading = isChangingLostMode
                                    )
                                } else {
                                    Text(
                                        "Activa el Modo Perdido si tu mascota está extraviada.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = AppColors.TextSecondary
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Button(
                                        onClick = { showLostModeConfirm = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.Coral),
                                        modifier = Modifier.fillMaxWidth(),
                                        enabled = !isChangingLostMode
                                    ) {
                                        if (isChangingLostMode) {
                                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = AppColors.TextOnDark, strokeWidth = 2.dp)
                                        } else {
                                            Text("Activar Modo Perdido")
                                        }
                                    }
                                }
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("⭐", style = MaterialTheme.typography.bodyMedium)
                                    Column {
                                        Text("Solo disponible en Premium", style = MaterialTheme.typography.titleMedium)
                                        Text(
                                            "Actualiza tu plan para usar Modo Perdido.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = AppColors.TextSecondary
                                        )
                                    }
                                }
                                Spacer(Modifier.height(8.dp))
                                Button(
                                    onClick = onNavigateToPremium,
                                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.Mustard),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Ver planes Premium")
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))
                    }
                }
            }

            if (isChangingLostMode) {
                LoadingOverlay("Actualizando…")
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = AppColors.TextSecondary)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = AppColors.TextPrimary, textAlign = TextAlign.End)
    }
}
