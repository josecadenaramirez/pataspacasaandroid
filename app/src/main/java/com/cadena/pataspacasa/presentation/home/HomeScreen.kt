package com.cadena.pataspacasa.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.cadena.pataspacasa.domain.entity.Pet
import com.cadena.pataspacasa.domain.entity.PetSpecies
import com.cadena.pataspacasa.presentation.common.*
import com.cadena.pataspacasa.presentation.theme.AppColors
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToPetDetail: (String) -> Unit,
    onNavigateToAddPet: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.loadData()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    val isRefreshing = uiState is HomeUiState.Loading

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                actions = {
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(Icons.Default.Person, contentDescription = "Perfil", tint = AppColors.TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppColors.BackgroundPrimary)
            )
        },
        containerColor = AppColors.BackgroundPrimary
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                viewModel.refresh()
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header greeting
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        val firstName = userName.split(" ").firstOrNull() ?: userName
                        Text(
                            text = "Hola $firstName 👋",
                            style = MaterialTheme.typography.displaySmall
                        )
                        Text(
                            text = "Aquí están tus compañeros",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppColors.TextSecondary
                        )
                    }
                }

                // Pets section header
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Mis mascotas",
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                when (val state = uiState) {
                    is HomeUiState.Loading -> {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = AppColors.Primary)
                            }
                        }
                    }
                    is HomeUiState.Success -> {
                        items(state.pets, key = { it.id }) { pet ->
                            PetCard(pet = pet, onClick = { onNavigateToPetDetail(pet.id) })
                        }
                        item {
                            Spacer(Modifier.height(8.dp))
                            PrimaryButton(title = "Agregar mascota", onClick = onNavigateToAddPet)
                        }
                    }
                    is HomeUiState.Empty -> {
                        item {
                            EmptyStateView(
                                icon = "🐾",
                                title = "Aún no tienes mascotas",
                                description = "Registra a tu primer compañero y empieza a protegerlo con una placa NFC.",
                                actionTitle = "Registrar mi primera mascota",
                                onAction = onNavigateToAddPet
                            )
                        }
                    }
                    is HomeUiState.Error -> {
                        item {
                            ErrorView(
                                message = state.message,
                                onRetry = { viewModel.retry() }
                            )
                        }
                    }
                    is HomeUiState.Offline -> {
                        item {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text("📵", style = MaterialTheme.typography.displaySmall)
                                Text("Sin conexión", style = MaterialTheme.typography.headlineSmall)
                                Text(
                                    "Revisa tu conexión a internet.",
                                    color = AppColors.TextSecondary,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                OutlinedButton(onClick = { viewModel.retry() }) { Text("Reintentar") }
                            }
                        }
                    }
                    is HomeUiState.Unauthorized -> {
                        item {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text("🔒", style = MaterialTheme.typography.displaySmall)
                                Text("Sesión expirada", style = MaterialTheme.typography.headlineSmall)
                                Text(
                                    "Inicia sesión de nuevo para continuar.",
                                    color = AppColors.TextSecondary,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PetCard(
    pet: Pet,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.BackgroundSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(AppColors.BackgroundSecondary),
                contentAlignment = Alignment.Center
            ) {
                if (!pet.photoUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = pet.photoUrl,
                        contentDescription = pet.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = when (pet.species) {
                            PetSpecies.DOG -> "🐕"
                            PetSpecies.CAT -> "🐈"
                            PetSpecies.OTHER -> "🐾"
                        },
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = pet.name,
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (pet.isLost) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = AppColors.BadgeLost
                        ) {
                            Text(
                                text = "Perdido",
                                style = MaterialTheme.typography.labelSmall,
                                color = AppColors.TextOnDark,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Text(
                    text = buildString {
                        if (pet.breed.isNotEmpty()) append(pet.breed)
                        if (pet.breed.isNotEmpty() && pet.species != PetSpecies.OTHER) append(" · ")
                        if (pet.species != PetSpecies.OTHER) append(pet.species.displayName)
                    }.ifEmpty { pet.species.displayName },
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!pet.hasNFCTag) {
                    Text(
                        text = "Sin placa NFC",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.TextDisabled
                    )
                }
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = AppColors.TextDisabled
            )
        }
    }
}
