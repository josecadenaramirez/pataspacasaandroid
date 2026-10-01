package com.cadena.pataspacasa.presentation.pet

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Nfc
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
fun LinkTagScreen(
    initialTagCode: String = "",
    viewModel: LinkTagViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onClaimSuccess: () -> Unit
) {
    val step by viewModel.step.collectAsState()
    val tagInput by viewModel.tagInput.collectAsState()

    LaunchedEffect(initialTagCode) {
        if (initialTagCode.isNotEmpty()) {
            viewModel.setInitialTagCode(initialTagCode)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(when (step) {
                        is LinkTagStep.Entry -> "Agregar mascota"
                        is LinkTagStep.Claiming -> "Agregando…"
                        is LinkTagStep.Success -> ""
                        is LinkTagStep.Failure -> "Error al agregar"
                    })
                },
                navigationIcon = {
                    if (step !is LinkTagStep.Claiming) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppColors.BackgroundPrimary)
            )
        },
        containerColor = AppColors.BackgroundPrimary
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when (val s = step) {
                is LinkTagStep.Entry -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        Spacer(Modifier.height(32.dp))

                        Surface(
                            shape = androidx.compose.foundation.shape.CircleShape,
                            color = AppColors.Primary.copy(alpha = 0.1f),
                            modifier = Modifier.size(100.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Nfc, null, modifier = Modifier.size(48.dp), tint = AppColors.Primary)
                            }
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Pegar enlace o código", style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
                            Text(
                                "Pega el enlace que aparece en el perfil público de tu mascota",
                                style = MaterialTheme.typography.bodySmall,
                                color = AppColors.TextSecondary,
                                textAlign = TextAlign.Center
                            )
                        }

                        AppTextField(
                            label = "Enlace o código",
                            value = tagInput,
                            onValueChange = viewModel::onTagInputChange,
                            placeholder = "Ej. aBc123 o https://pataspacasa.com/p/aBc123"
                        )

                        PrimaryButton(
                            title = "Agregar a mi cuenta",
                            onClick = { viewModel.claimPet() },
                            isLoading = false
                        )

                        Spacer(Modifier.height(32.dp))
                    }
                }
                is LinkTagStep.Claiming -> LoadingView("Vinculando mascota…")
                is LinkTagStep.Success -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Spacer(Modifier.height(48.dp))
                        Text("✅", style = MaterialTheme.typography.displaySmall)
                        Text("¡Mascota agregada!", style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
                        Text(
                            "${s.pet.name} ya es parte de tu cuenta.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppColors.TextSecondary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(16.dp))
                        PrimaryButton(title = "Ir a mis mascotas", onClick = onClaimSuccess)
                    }
                }
                is LinkTagStep.Failure -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Spacer(Modifier.height(48.dp))
                        ErrorView(
                            message = s.message,
                            retryTitle = "Intentar de nuevo",
                            onRetry = { viewModel.retry() }
                        )
                    }
                }
            }
        }
    }
}
