package com.cadena.pataspacasa.presentation.user

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cadena.pataspacasa.presentation.theme.AppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit
) {
    val userName by viewModel.userName.collectAsState()
    val email by viewModel.email.collectAsState()
    val isProcessing by viewModel.isProcessing.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    var showLogoutConfirm by remember { mutableStateOf(false) }
    var showPasswordResetConfirm by remember { mutableStateOf(false) }

    successMessage?.let { msg ->
        AlertDialog(
            onDismissRequest = { viewModel.clearSuccess() },
            title = { Text("Correo enviado") },
            text = { Text(msg) },
            confirmButton = { TextButton(onClick = { viewModel.clearSuccess() }) { Text("OK") } }
        )
    }

    errorMessage?.let { msg ->
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = { Text("Error") },
            text = { Text(msg) },
            confirmButton = { TextButton(onClick = { viewModel.clearError() }) { Text("OK") } }
        )
    }

    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            title = { Text("¿Cerrar sesión?") },
            text = { Text("Podrás volver a entrar con tu cuenta cuando quieras.") },
            confirmButton = {
                TextButton(onClick = { showLogoutConfirm = false; viewModel.signOut(onLogout) }) {
                    Text("Cerrar sesión", color = AppColors.Error)
                }
            },
            dismissButton = { TextButton(onClick = { showLogoutConfirm = false }) { Text("Cancelar") } }
        )
    }

    if (showPasswordResetConfirm) {
        AlertDialog(
            onDismissRequest = { showPasswordResetConfirm = false },
            title = { Text("Cambiar contraseña") },
            text = { Text("Te enviaremos un correo a $email para que puedas crear una nueva contraseña.") },
            confirmButton = {
                TextButton(onClick = { showPasswordResetConfirm = false; viewModel.sendPasswordReset() }) {
                    Text("Enviar correo")
                }
            },
            dismissButton = { TextButton(onClick = { showPasswordResetConfirm = false }) { Text("Cancelar") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi perfil") },
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
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar
            Surface(
                shape = CircleShape,
                color = AppColors.BackgroundSecondary,
                modifier = Modifier.size(84.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Person, null, modifier = Modifier.size(48.dp), tint = AppColors.TextSecondary)
                }
            }

            Spacer(Modifier.height(12.dp))
            Text(
                userName,
                style = MaterialTheme.typography.headlineSmall,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Text(
                email,
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.TextSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(24.dp))

            // Settings list
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "Cuenta",
                    style = MaterialTheme.typography.labelMedium,
                    color = AppColors.TextSecondary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
                Card(
                    onClick = { showPasswordResetConfirm = true },
                    enabled = !isProcessing && email.isNotEmpty(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = AppColors.BackgroundSurface)
                ) {
                    ListItem(
                        headlineContent = { Text("Cambiar contraseña") },
                        supportingContent = { Text("Recibe un enlace seguro por correo") },
                        colors = ListItemDefaults.colors(containerColor = AppColors.BackgroundSurface),
                        trailingContent = {
                            if (isProcessing) CircularProgressIndicator(Modifier.size(18.dp), color = AppColors.Primary, strokeWidth = 2.dp)
                        }
                    )
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    "Sesión",
                    style = MaterialTheme.typography.labelMedium,
                    color = AppColors.TextSecondary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )

                Card(
                    onClick = { showLogoutConfirm = true },
                    enabled = !isProcessing,
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = AppColors.Error.copy(alpha = 0.08f))
                ) {
                    Text(
                        "Cerrar sesión",
                        style = MaterialTheme.typography.bodyLarge,
                        color = AppColors.Error,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp)
                    )
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}
