package com.cadena.pataspacasa.presentation.pet

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cadena.pataspacasa.domain.entity.PetSpecies
import com.cadena.pataspacasa.presentation.common.*
import com.cadena.pataspacasa.presentation.theme.AppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPetScreen(
    petId: String,
    viewModel: EditPetViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToPremium: () -> Unit
) {
    val loadState by viewModel.loadState.collectAsState()
    val saveState by viewModel.saveState.collectAsState()
    val name by viewModel.name.collectAsState()
    val species by viewModel.species.collectAsState()
    val breed by viewModel.breed.collectAsState()
    val breedOptions by viewModel.breedOptions.collectAsState()
    val ownerName by viewModel.ownerName.collectAsState()
    val phonePrimary by viewModel.phonePrimary.collectAsState()
    val phoneSecondary1 by viewModel.phoneSecondary1.collectAsState()
    val phoneSecondary2 by viewModel.phoneSecondary2.collectAsState()
    val allergies by viewModel.allergies.collectAsState()
    val treatment by viewModel.treatment.collectAsState()
    val nameError by viewModel.nameError.collectAsState()
    val breedError by viewModel.breedError.collectAsState()
    val phonesError by viewModel.phonesError.collectAsState()

    LaunchedEffect(petId) { viewModel.setPetId(petId) }

    if (saveState == EditPetSaveState.PERMISSION_DENIED) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Premium requerido") },
            text = { Text("Esta función está disponible solo en el plan Premium.") },
            confirmButton = {
                TextButton(onClick = onNavigateToPremium) { Text("Ver Premium") }
            },
            dismissButton = {
                TextButton(onClick = onNavigateBack) { Text("Cancelar") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar mascota") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.save(onNavigateBack) },
                        enabled = saveState != EditPetSaveState.SAVING
                    ) {
                        if (saveState == EditPetSaveState.SAVING) {
                            CircularProgressIndicator(Modifier.size(18.dp), color = AppColors.Primary, strokeWidth = 2.dp)
                        } else {
                            Text("Guardar", color = AppColors.Primary)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppColors.BackgroundPrimary)
            )
        },
        containerColor = AppColors.BackgroundPrimary
    ) { paddingValues ->
        when (val state = loadState) {
            is EditPetLoadState.Loading -> LoadingView("Cargando…")
            is EditPetLoadState.Error -> ErrorView(state.message, onRetry = { viewModel.retry() })
            is EditPetLoadState.Success -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SectionCard("Datos de la mascota") {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            AppTextField(
                                label = "Nombre *",
                                value = name,
                                onValueChange = viewModel::onNameChange,
                                placeholder = "Ej. Max",
                                errorMessage = nameError
                            )
                            var speciesExpanded by remember { mutableStateOf(false) }
                            ExposedDropdownMenuBox(
                                expanded = speciesExpanded,
                                onExpandedChange = { speciesExpanded = !speciesExpanded }
                            ) {
                                OutlinedTextField(
                                    value = species.displayName,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Tipo *") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = speciesExpanded) },
                                    modifier = Modifier.menuAnchor().fillMaxWidth()
                                )
                                ExposedDropdownMenu(
                                    expanded = speciesExpanded,
                                    onDismissRequest = { speciesExpanded = false }
                                ) {
                                    PetSpecies.values().forEach { option ->
                                        DropdownMenuItem(
                                            text = { Text(option.displayName) },
                                            onClick = {
                                                speciesExpanded = false
                                                viewModel.onSpeciesChange(option)
                                            }
                                        )
                                    }
                                }
                            }

                            if (species == PetSpecies.DOG || species == PetSpecies.CAT) {
                                var breedExpanded by remember { mutableStateOf(false) }
                                ExposedDropdownMenuBox(
                                    expanded = breedExpanded,
                                    onExpandedChange = { breedExpanded = !breedExpanded }
                                ) {
                                    OutlinedTextField(
                                        value = breed,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Raza *") },
                                        placeholder = { Text("Selecciona una raza") },
                                        isError = breedError != null,
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = breedExpanded) },
                                        modifier = Modifier.menuAnchor().fillMaxWidth()
                                    )
                                    ExposedDropdownMenu(
                                        expanded = breedExpanded,
                                        onDismissRequest = { breedExpanded = false }
                                    ) {
                                        breedOptions.forEach { option ->
                                            DropdownMenuItem(
                                                text = { Text(option) },
                                                onClick = {
                                                    breedExpanded = false
                                                    viewModel.onBreedChange(option)
                                                }
                                            )
                                        }
                                    }
                                }
                                breedError?.let {
                                    Text(it, style = MaterialTheme.typography.bodySmall, color = AppColors.Error)
                                }
                            } else {
                                AppTextField(
                                    label = "Raza",
                                    value = breed,
                                    onValueChange = viewModel::onBreedChange,
                                    placeholder = "Opcional"
                                )
                            }
                            AppTextField(
                                label = "Nombre del dueño (visible en placa)",
                                value = ownerName,
                                onValueChange = viewModel::onOwnerNameChange,
                                placeholder = "Tu nombre"
                            )
                        }
                    }

                    SectionCard("Teléfonos de contacto") {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            AppTextField(
                                label = "Celular principal *",
                                value = phonePrimary,
                                onValueChange = viewModel::onPhonePrimaryChange,
                                placeholder = "+52 33 1234 5678",
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                            )
                            AppTextField(
                                label = "Celular adicional",
                                value = phoneSecondary1,
                                onValueChange = viewModel::onPhoneSecondary1Change,
                                placeholder = "+52 33 1234 5678",
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                            )
                            AppTextField(
                                label = "Celular adicional",
                                value = phoneSecondary2,
                                onValueChange = viewModel::onPhoneSecondary2Change,
                                placeholder = "+52 33 1234 5678",
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                            )
                            if (phonesError != null) {
                                Text(phonesError!!, style = MaterialTheme.typography.bodySmall, color = AppColors.Error)
                            }
                            Text(
                                "Usa un número de celular con lada.",
                                style = MaterialTheme.typography.bodySmall,
                                color = AppColors.TextSecondary
                            )
                        }
                    }

                    SectionCard("Salud") {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            AppTextField(
                                label = "Alergias",
                                value = allergies,
                                onValueChange = viewModel::onAllergiesChange,
                                placeholder = "Ej. Pollo, Polen"
                            )
                            AppTextField(
                                label = "Medicamentos o tratamientos",
                                value = treatment,
                                onValueChange = viewModel::onTreatmentChange,
                                placeholder = "Ej. Antiparasitario mensual",
                                singleLine = false
                            )
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    PrimaryButton(
                        title = "Guardar cambios",
                        onClick = { viewModel.save(onNavigateBack) },
                        isLoading = saveState == EditPetSaveState.SAVING
                    )

                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}
