package com.cadena.pataspacasa.presentation.pet

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cadena.pataspacasa.MainActivity
import com.cadena.pataspacasa.core.config.AppConfiguration
import com.cadena.pataspacasa.presentation.common.AppTextField
import com.cadena.pataspacasa.presentation.common.PrimaryButton
import com.cadena.pataspacasa.presentation.theme.AppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPetScreen(
    viewModel: AddPetViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToPetDetail: (String) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val input by viewModel.input.collectAsState()
    val activity = LocalContext.current as? MainActivity
    val working = state == AddPetState.Scanning || state == AddPetState.Linking

    DisposableEffect(Unit) { onDispose { activity?.cancelNfcScan() } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Agregar mascota") },
                navigationIcon = {
                    if (!working) IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppColors.BackgroundPrimary)
            )
        },
        containerColor = AppColors.BackgroundPrimary
    ) { padding ->
        when (val current = state) {
            AddPetState.Idle -> Column(
                modifier = Modifier.fillMaxSize().padding(padding).imePadding().verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Icon(Icons.Default.Nfc, null, tint = AppColors.Primary, modifier = Modifier.size(88.dp))
                Text("Vincular una placa", style = MaterialTheme.typography.headlineSmall)
                Text(
                    "Escanea la placa o pega su código para asociarla a tu cuenta.",
                    color = AppColors.TextSecondary,
                    textAlign = TextAlign.Center
                )
                AppTextField(
                    label = "ID o enlace",
                    value = input,
                    onValueChange = viewModel::onInputChange,
                    placeholder = "aBc123 o ${AppConfiguration.examplePetUrl()}"
                )
                PrimaryButton(
                    title = "Vincular a mi cuenta",
                    onClick = viewModel::link,
                    enabled = input.trim().isNotEmpty()
                )
                OutlinedButton(
                    onClick = {
                        viewModel.beginScan()
                        activity?.scanNfc { result ->
                            result.fold(viewModel::scanned) { viewModel.scanFailed() }
                        } ?: viewModel.scanFailed()
                    },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
                ) {
                    Icon(Icons.Default.Nfc, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Escanear placa NFC")
                }
            }
            AddPetState.Scanning -> Working("Acerca tu teléfono a la placa…", padding)
            AddPetState.Linking -> Working("Vinculando mascota…", padding)
            is AddPetState.Failure -> Column(
                modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Spacer(Modifier.height(32.dp))
                Text(current.message, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.error)
                PrimaryButton(title = "Intentar de nuevo", onClick = viewModel::retry)
            }
            is AddPetState.Success -> Column(
                modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Spacer(Modifier.height(32.dp))
                Text("Mascota vinculada", style = MaterialTheme.typography.headlineMedium)
                Text(
                    "${current.pet.name} ya aparece en tu cuenta.",
                    color = AppColors.TextSecondary,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(28.dp))
                PrimaryButton(title = "Ver detalle", onClick = { onNavigateToPetDetail(current.pet.id) })
                TextButton(onClick = viewModel::startAnother) { Text("Vincular otra placa") }
            }
        }
    }
}

@Composable
private fun Working(message: String, padding: PaddingValues) {
    Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(Modifier.height(16.dp))
            Text(message)
        }
    }
}
