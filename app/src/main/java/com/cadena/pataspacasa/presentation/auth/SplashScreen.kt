package com.cadena.pataspacasa.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cadena.pataspacasa.presentation.theme.AppColors

@Composable
fun SplashScreen(
    viewModel: SplashViewModel = hiltViewModel(),
    onAuthResolved: (isAuthenticated: Boolean) -> Unit
) {
    val isAuthenticated by viewModel.isAuthenticated.collectAsState()

    LaunchedEffect(isAuthenticated) {
        val auth = isAuthenticated
        if (auth != null) {
            onAuthResolved(auth)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Primary),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Pets,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = AppColors.BackgroundSurface
            )
            Text(
                text = "Patas pa' Casa",
                style = MaterialTheme.typography.displaySmall,
                color = AppColors.BackgroundSurface
            )
            Spacer(Modifier.height(32.dp))
            CircularProgressIndicator(
                color = AppColors.BackgroundSurface.copy(alpha = 0.7f)
            )
        }
    }
}
