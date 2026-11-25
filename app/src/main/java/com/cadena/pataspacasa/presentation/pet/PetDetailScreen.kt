package com.cadena.pataspacasa.presentation.pet

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.cadena.pataspacasa.domain.entity.PetSpecies
import com.cadena.pataspacasa.core.error.AppErrorMapper
import com.cadena.pataspacasa.presentation.common.*
import com.cadena.pataspacasa.presentation.theme.AppColors
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetDetailScreen(
    petId: String,
    viewModel: PetDetailViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToEditPet: (String) -> Unit,
    onNavigateToLostMode: (String) -> Unit,
    onNavigateToLinkTag: (String) -> Unit,
    onNavigateToScanHistory: (String, String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val isChangingLostMode by viewModel.isChangingLostMode.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    var showDisableLostModeConfirm by remember { mutableStateOf(false) }
    var showImageGallery by remember { mutableStateOf(false) }
    var imageGalleryLoading by remember { mutableStateOf(false) }
    var imageGalleryAccess by remember { mutableStateOf(ImageMediaAccess.NONE) }
    var localImages by remember { mutableStateOf<List<LocalDeviceImage>>(emptyList()) }
    var localGalleryMessage by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    val loadLocalGallery: () -> Unit = {
        imageGalleryAccess = currentImageMediaAccess(context)
        showImageGallery = true
        imageGalleryLoading = true
        coroutineScope.launch {
            when (val result = loadLocalDeviceImages(context)) {
                is LocalImageLoadResult.Success -> localImages = result.images
                is LocalImageLoadResult.Error -> {
                    localImages = emptyList()
                    localGalleryMessage = result.message
                }
            }
            imageGalleryLoading = false
        }
    }
    val imagePermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        if (currentImageMediaAccess(context) != ImageMediaAccess.NONE) {
            loadLocalGallery()
        } else {
            localGalleryMessage = "Necesitamos permiso para mostrar las imágenes guardadas en este dispositivo."
        }
    }

    LaunchedEffect(petId) { viewModel.setPetId(petId) }
    DisposableEffect(lifecycleOwner, petId) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.loadPet()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    errorMessage?.let { msg ->
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = { Text("Error") },
            text = { Text(msg) },
            confirmButton = { TextButton(onClick = { viewModel.clearError() }) { Text("OK") } }
        )
    }

    localGalleryMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { localGalleryMessage = null },
            title = { Text("No se pudo abrir la galería") },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = { localGalleryMessage = null }) { Text("Entendido") }
            }
        )
    }

    if (showImageGallery) {
        LocalImageGallerySheet(
            images = localImages,
            isLoading = imageGalleryLoading,
            access = imageGalleryAccess,
            onDismiss = { showImageGallery = false },
            onImageSelected = { image ->
                showImageGallery = false
                coroutineScope.launch {
                    runCatching { preparePetPhoto(context, image.uri) }
                        .onSuccess { photo -> viewModel.updatePhoto(photo.bytes) }
                        .onFailure { error ->
                            localGalleryMessage = AppErrorMapper.from(error).userMessage
                        }
                }
            }
        )
    }

    val pet = (uiState as? PetDetailUiState.Success)?.pet

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
                        if (p.isLost) {
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
                                    Column(modifier = Modifier.weight(1f)) {
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
                                modifier = Modifier.size(136.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(120.dp)
                                        .clip(CircleShape)
                                        .background(AppColors.BackgroundSecondary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (!p.photoUrl.isNullOrEmpty()) {
                                        AsyncImage(
                                            model = ImageRequest.Builder(context)
                                                .data(p.photoUrl)
                                                .memoryCacheKey("${p.photoUrl}:${p.updatedAt.time}")
                                                .diskCacheKey("${p.photoUrl}:${p.updatedAt.time}")
                                                .crossfade(true)
                                                .build(),
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
                                }
                                if (p.canUpdatePhoto) {
                                    FilledIconButton(
                                        onClick = {
                                            if (currentImageMediaAccess(context) != ImageMediaAccess.NONE) {
                                                loadLocalGallery()
                                            } else {
                                                imagePermissionLauncher.launch(requiredImagePermissions())
                                            }
                                        },
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .size(48.dp)
                                            .semantics { contentDescription = "Cambiar foto" },
                                        colors = IconButtonDefaults.filledIconButtonColors(
                                            containerColor = AppColors.Primary,
                                            contentColor = AppColors.TextOnDark
                                        )
                                    ) {
                                        Icon(
                                            Icons.Default.CameraAlt,
                                            contentDescription = null,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(p.name, style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
                            Text(
                                "${p.breed.ifEmpty { "Raza desconocida" }} · ${p.species.displayName}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = AppColors.TextSecondary,
                                textAlign = TextAlign.Center
                            )
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

                        if (p.hasNFCTag) {
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
                                    "Activa el Modo Perdido para alertar a quien encuentre a ${p.name} y registrar los escaneos de su placa.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = AppColors.TextSecondary
                                )
                                Spacer(Modifier.height(8.dp))
                                Button(
                                    onClick = { onNavigateToLostMode(p.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.Coral),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Activar Modo Perdido")
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
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = AppColors.TextSecondary,
            modifier = Modifier.weight(1f)
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            color = AppColors.TextPrimary,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1.4f)
        )
    }
}
