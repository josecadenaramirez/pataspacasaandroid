package com.cadena.pataspacasa.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cadena.pataspacasa.presentation.common.AppTextField
import com.cadena.pataspacasa.presentation.common.PrimaryButton
import com.cadena.pataspacasa.presentation.theme.AppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel = hiltViewModel(),
    onRegisterSuccess: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }

    uiState.error?.let { error ->
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = { Text("No pudimos crear tu cuenta") },
            text = { Text(error.userMessage) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) { Text("Entendido") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear cuenta") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppColors.BackgroundPrimary
                )
            )
        },
        containerColor = AppColors.BackgroundPrimary
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("Crea tu cuenta", style = MaterialTheme.typography.headlineSmall)
                Text(
                    "Únete y registra a tus compañeros peludos 🐾",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.TextSecondary,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(32.dp))

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                AppTextField(
                    label = "Nombre completo",
                    value = uiState.name,
                    onValueChange = { viewModel.onNameChange(it); viewModel.validateNameOnChange() },
                    placeholder = "Tu nombre",
                    errorMessage = uiState.nameError
                )
                AppTextField(
                    label = "Correo electrónico",
                    value = uiState.email,
                    onValueChange = viewModel::onEmailChange,
                    placeholder = "hola@ejemplo.com",
                    errorMessage = uiState.emailError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )
                AppTextField(
                    label = "Contraseña",
                    value = uiState.password,
                    onValueChange = viewModel::onPasswordChange,
                    placeholder = "Mínimo 8 caracteres",
                    errorMessage = uiState.passwordError,
                    isPassword = !showPassword,
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (showPassword) "Ocultar contraseña" else "Mostrar contraseña"
                            )
                        }
                    }
                )
                AppTextField(
                    label = "Confirmar contraseña",
                    value = uiState.confirmPassword,
                    onValueChange = viewModel::onConfirmPasswordChange,
                    placeholder = "Repite tu contraseña",
                    errorMessage = uiState.confirmPasswordError,
                    isPassword = !showConfirmPassword,
                    trailingIcon = {
                        IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                            Icon(
                                if (showConfirmPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (showConfirmPassword) "Ocultar contraseña" else "Mostrar contraseña"
                            )
                        }
                    }
                )
                Text(
                    "Mínimo 8 caracteres con al menos una mayúscula.",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.TextSecondary
                )
            }

            Spacer(Modifier.height(24.dp))

            PrimaryButton(
                title = "Crear cuenta",
                onClick = { viewModel.register(onRegisterSuccess) },
                isLoading = uiState.isLoading
            )

            Spacer(Modifier.height(32.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("¿Ya tienes cuenta?", style = MaterialTheme.typography.bodyMedium, color = AppColors.TextSecondary)
                TextButton(onClick = onNavigateBack) {
                    Text("Inicia sesión", color = AppColors.Primary, style = MaterialTheme.typography.labelLarge)
                }
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}
