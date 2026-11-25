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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditPetScreen(
    petId: String,
    viewModel: EditPetViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val loadState by viewModel.loadState.collectAsState()
    val saveState by viewModel.saveState.collectAsState()
    val name by viewModel.name.collectAsState()
    val species by viewModel.species.collectAsState()
    val breed by viewModel.breed.collectAsState()
    val breedOptions by viewModel.breedOptions.collectAsState()
    val customPetType by viewModel.customPetType.collectAsState()
    val sex by viewModel.sex.collectAsState()
    val sterilized by viewModel.sterilized.collectAsState()
    val ownerName by viewModel.ownerName.collectAsState()
    val ownerEmail by viewModel.ownerEmail.collectAsState()
    val address by viewModel.address.collectAsState()
    val phonePrimary by viewModel.phonePrimary.collectAsState()
    val phoneSecondary1 by viewModel.phoneSecondary1.collectAsState()
    val phoneSecondary2 by viewModel.phoneSecondary2.collectAsState()
    val allergies by viewModel.allergies.collectAsState()
    val treatment by viewModel.treatment.collectAsState()
    val nameError by viewModel.nameError.collectAsState()
    val breedError by viewModel.breedError.collectAsState()
    val phonesError by viewModel.phonesError.collectAsState()
    val saveError by viewModel.saveError.collectAsState()
    val canSave = name.trim().isNotEmpty() &&
        (species == PetSpecies.OTHER || breed.trim().isNotEmpty()) &&
        saveState != EditPetSaveState.SAVING

    LaunchedEffect(petId) { viewModel.setPetId(petId) }

    if (saveState == EditPetSaveState.PERMISSION_DENIED) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Sin permiso") },
            text = { Text("No tienes permiso para editar esta mascota.") },
            confirmButton = {
                TextButton(onClick = onNavigateBack) { Text("Entendido") }
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
                        enabled = canSave
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
            is EditPetLoadState.Loading -> Box(Modifier.fillMaxSize().padding(paddingValues)) {
                LoadingView("Cargando…")
            }
            is EditPetLoadState.Error -> Box(Modifier.fillMaxSize().padding(paddingValues)) {
                ErrorView(state.message, onRetry = { viewModel.retry() })
            }
            is EditPetLoadState.Success -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .imePadding()
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
                                if (breed.isNotBlank() && breedOptions.none { it.equals(breed, ignoreCase = true) }) {
                                    Text(
                                        "$breed ya no está disponible en el catálogo.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = AppColors.Error
                                    )
                                }
                            } else {
                                AppTextField(
                                    label = "Tipo de mascota",
                                    value = customPetType,
                                    onValueChange = viewModel::onCustomPetTypeChange,
                                    placeholder = "Ej. Conejo"
                                )
                            }
                            var sexExpanded by remember { mutableStateOf(false) }
                            ExposedDropdownMenuBox(
                                expanded = sexExpanded,
                                onExpandedChange = { sexExpanded = !sexExpanded }
                            ) {
                                OutlinedTextField(
                                    value = when (sex) { "male" -> "Macho"; "female" -> "Hembra"; else -> "Prefiero no decirlo" },
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Sexo") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(sexExpanded) },
                                    modifier = Modifier.menuAnchor().fillMaxWidth()
                                )
                                ExposedDropdownMenu(expanded = sexExpanded, onDismissRequest = { sexExpanded = false }) {
                                    listOf("male" to "Macho", "female" to "Hembra", "" to "Prefiero no decirlo").forEach { option ->
                                        DropdownMenuItem(text = { Text(option.second) }, onClick = {
                                            sexExpanded = false
                                            viewModel.onSexChange(option.first)
                                        })
                                    }
                                }
                            }
                            Text("¿Está esterilizada?", style = MaterialTheme.typography.labelLarge)
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                listOf(true to "Sí", false to "No", null to "Sin especificar").forEach { option ->
                                    FilterChip(
                                        selected = sterilized == option.first,
                                        onClick = { viewModel.onSterilizedChange(option.first) },
                                        label = { Text(option.second) }
                                    )
                                }
                            }
                            AppTextField(
                                label = "Nombre del dueño (visible en placa)",
                                value = ownerName,
                                onValueChange = viewModel::onOwnerNameChange,
                                placeholder = "Tu nombre"
                            )
                            AppTextField(
                                label = "Correo del dueño",
                                value = ownerEmail,
                                onValueChange = viewModel::onOwnerEmailChange,
                                placeholder = "hola@ejemplo.com",
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                            )
                            AppTextField(
                                label = "Dirección",
                                value = address,
                                onValueChange = viewModel::onAddressChange,
                                placeholder = "Opcional"
                            )
                        }
                    }

                    SectionCard("Teléfonos de contacto") {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            AppTextField(
                                label = "Celular principal",
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

                    saveError?.let {
                        Text(it, style = MaterialTheme.typography.bodySmall, color = AppColors.Error)
                    }

                    Spacer(Modifier.height(24.dp))

                    PrimaryButton(
                        title = "Guardar cambios",
                        onClick = { viewModel.save(onNavigateBack) },
                        isLoading = saveState == EditPetSaveState.SAVING,
                        enabled = canSave
                    )

                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}
