package com.cadena.pataspacasa.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pets
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

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showPassword by remember { mutableStateOf(false) }

    // Show error dialog
    uiState.error?.let { error ->
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = { Text("No pudimos iniciar sesión") },
            text = { Text(error.userMessage) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) { Text("Entendido") }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.BackgroundPrimary)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(80.dp))

        // Header
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Surface(
                shape = androidx.compose.foundation.shape.CircleShape,
                color = AppColors.Primary.copy(alpha = 0.1f),
                modifier = Modifier.size(88.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Pets,
                        contentDescription = null,
                        modifier = Modifier.size(44.dp),
                        tint = AppColors.Primary
                    )
                }
            }
            Text("Patas pa' Casa", style = MaterialTheme.typography.displaySmall)
            Text(
                "Inicia sesión para cuidar a tus mascotas",
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.TextSecondary,
                textAlign = TextAlign.Center
            )
        }

        Spacer(Modifier.height(40.dp))

        // Fields
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            AppTextField(
                label = "Correo electrónico",
                value = uiState.email,
                onValueChange = { viewModel.onEmailChange(it); viewModel.validateEmailOnChange() },
                placeholder = "hola@ejemplo.com",
                errorMessage = uiState.emailError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )
            AppTextField(
                label = "Contraseña",
                value = uiState.password,
                onValueChange = viewModel::onPasswordChange,
                placeholder = "Tu contraseña",
                errorMessage = uiState.passwordError,
                isPassword = !showPassword,
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null
                        )
                    }
                }
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { onNavigateToForgotPassword(uiState.email) }) {
                    Text("¿Olvidaste tu contraseña?", color = AppColors.Primary, style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // Actions
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            PrimaryButton(
                title = "Iniciar sesión",
                onClick = { viewModel.signIn(onLoginSuccess) },
                isLoading = uiState.isLoading
            )
        }

        Spacer(Modifier.height(32.dp))

        // Register link
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("¿No tienes cuenta?", style = MaterialTheme.typography.bodyMedium, color = AppColors.TextSecondary)
            TextButton(onClick = onNavigateToRegister) {
                Text("Regístrate", color = AppColors.Primary, style = MaterialTheme.typography.labelLarge)
            }
        }

        Spacer(Modifier.height(40.dp))
    }
}
