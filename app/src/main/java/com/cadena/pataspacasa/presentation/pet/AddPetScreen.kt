package com.cadena.pataspacasa.presentation.pet

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cadena.pataspacasa.presentation.common.*
import com.cadena.pataspacasa.presentation.theme.AppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPetScreen(
    viewModel: AddPetViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToLinkTag: (String) -> Unit
) {
    val saveState by viewModel.saveState.collectAsState()
    val name by viewModel.name.collectAsState()
    val breed by viewModel.breed.collectAsState()
    val phonePrimary by viewModel.phonePrimary.collectAsState()
    val nameError by viewModel.nameError.collectAsState()
    val phonesError by viewModel.phonesError.collectAsState()

    if (saveState is AddPetSaveState.Error) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Error") },
            text = { Text((saveState as AddPetSaveState.Error).message) },
            confirmButton = { TextButton(onClick = {}) { Text("OK") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Agregar mascota") },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("Agrega una mascota", style = MaterialTheme.typography.headlineSmall)
                Text(
                    "Agrega una mascota que ya tiene una placa Patas Pa' Casa.",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.TextSecondary,
                    textAlign = TextAlign.Center
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // NFC scan option
                Button(
                    onClick = { onNavigateToLinkTag("") },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Nfc, null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Acercar placa NFC")
                }

                // Manual code option
                OutlinedButton(
                    onClick = { onNavigateToLinkTag("") },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Link, null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Pegar enlace o código")
                }
            }

            HorizontalDivider(color = AppColors.Divider)

            // Or create new pet manually
            Text(
                "¿No tienes una placa aún? Crea el perfil de tu mascota:",
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.TextSecondary,
                textAlign = TextAlign.Center
            )

            SectionCard("Datos básicos") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    AppTextField(
                        label = "Nombre *",
                        value = name,
                        onValueChange = viewModel::onNameChange,
                        placeholder = "Ej. Max",
                        errorMessage = nameError
                    )
                    AppTextField(
                        label = "Raza",
                        value = breed,
                        onValueChange = viewModel::onBreedChange,
                        placeholder = "Ej. Labrador Dorado"
                    )
                    AppTextField(
                        label = "Teléfono de contacto *",
                        value = phonePrimary,
                        onValueChange = viewModel::onPhonePrimaryChange,
                        placeholder = "+52 33 1234 5678",
                        errorMessage = phonesError,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )
                }
            }

            PrimaryButton(
                title = "Crear perfil de mascota",
                onClick = { viewModel.createPet { } },
                isLoading = saveState is AddPetSaveState.Saving
            )

            Spacer(Modifier.height(16.dp))
        }
    }
}
