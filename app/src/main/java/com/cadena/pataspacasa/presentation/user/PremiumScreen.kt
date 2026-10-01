package com.cadena.pataspacasa.presentation.user

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cadena.pataspacasa.domain.entity.SubscriptionProduct
import com.cadena.pataspacasa.presentation.common.*
import com.cadena.pataspacasa.presentation.theme.AppColors
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumScreen(
    viewModel: PremiumViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val selectedProductId by viewModel.selectedProductId.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadData() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Plan Premium") },
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
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when (val s = state) {
                is PremiumViewState.Loading, is PremiumViewState.Purchasing -> LoadingView(
                    if (state is PremiumViewState.Purchasing) "Procesando…" else "Cargando planes…"
                )

                is PremiumViewState.AlreadyPremium -> AlreadyPremiumContent(
                    subscription = s.subscription,
                    onClose = onNavigateBack
                )

                is PremiumViewState.Available -> AvailableContent(
                    products = s.products,
                    selectedProductId = selectedProductId,
                    onSelectProduct = viewModel::selectProduct,
                    onPurchase = { viewModel.purchase() },
                    onRestore = { viewModel.restorePurchases() }
                )

                is PremiumViewState.Success -> SuccessPremiumContent(
                    subscription = s.subscription,
                    onClose = onNavigateBack
                )

                is PremiumViewState.Restored -> SuccessPremiumContent(
                    subscription = s.subscription,
                    title = "¡Suscripción restaurada!",
                    onClose = onNavigateBack
                )

                is PremiumViewState.Error -> ErrorView(
                    message = s.message,
                    retryTitle = "Reintentar",
                    onRetry = { viewModel.retry() }
                )
            }
        }
    }
}

@Composable
private fun AlreadyPremiumContent(
    subscription: com.cadena.pataspacasa.domain.entity.Subscription,
    onClose: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("d MMM yyyy", Locale("es", "MX")) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(Modifier.height(32.dp))
        PremiumHeroIcon(active = true)
        Text("¡Ya eres Premium! ⭐", style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
        Text("Tienes acceso completo a todas las funciones exclusivas.", style = MaterialTheme.typography.bodyMedium, color = AppColors.TextSecondary, textAlign = TextAlign.Center)

        SectionCard("Tu suscripción") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SubscriptionInfoRow("Plan", "Premium")
                SubscriptionInfoRow("Estado", subscription.status.displayName)
                subscription.expirationDate?.let {
                    SubscriptionInfoRow("Renovación", dateFormat.format(it))
                }
            }
        }

        BenefitsSection(active = true)
        PrimaryButton("Cerrar", onClick = onClose)
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun AvailableContent(
    products: List<SubscriptionProduct>,
    selectedProductId: String?,
    onSelectProduct: (String) -> Unit,
    onPurchase: () -> Unit,
    onRestore: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(Modifier.height(32.dp))
        PremiumHeroIcon(active = false)
        Text("Mejora a Premium", style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
        Text("Desbloquea todas las funciones para proteger mejor a tus mascotas.", style = MaterialTheme.typography.bodyMedium, color = AppColors.TextSecondary, textAlign = TextAlign.Center)

        // Products
        products.forEach { product ->
            Card(
                onClick = { onSelectProduct(product.id) },
                shape = RoundedCornerShape(16.dp),
                border = if (selectedProductId == product.id)
                    CardDefaults.outlinedCardBorder().copy(width = 2.dp)
                else null,
                colors = CardDefaults.cardColors(
                    containerColor = if (selectedProductId == product.id)
                        AppColors.Primary.copy(alpha = 0.05f)
                    else AppColors.BackgroundSurface
                )
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(product.displayName, style = MaterialTheme.typography.titleMedium)
                        Text(product.displayPrice + " / año", style = MaterialTheme.typography.bodySmall, color = AppColors.TextSecondary)
                    }
                    RadioButton(selected = selectedProductId == product.id, onClick = { onSelectProduct(product.id) }, colors = RadioButtonDefaults.colors(selectedColor = AppColors.Primary))
                }
            }
        }

        BenefitsSection(active = false)

        PrimaryButton("Suscribirse", onClick = onPurchase)

        TextButton(onClick = onRestore) {
            Text("Restaurar compra", color = AppColors.TextSecondary, style = MaterialTheme.typography.bodySmall)
        }

        Text(
            "Suscripción anual. Se renueva automáticamente. Cancela en cualquier momento desde Google Play.",
            style = MaterialTheme.typography.bodySmall,
            color = AppColors.TextDisabled,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun SuccessPremiumContent(
    subscription: com.cadena.pataspacasa.domain.entity.Subscription,
    title: String = "¡Bienvenido a Premium! ⭐",
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(Modifier.height(48.dp))
        PremiumHeroIcon(active = true)
        Text(title, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
        Text("Ahora tienes acceso a Modo Perdido, historial de escaneos y más.", style = MaterialTheme.typography.bodyMedium, color = AppColors.TextSecondary, textAlign = TextAlign.Center)
        Spacer(Modifier.height(16.dp))
        PrimaryButton("¡Genial!", onClick = onClose)
    }
}

@Composable
private fun PremiumHeroIcon(active: Boolean) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = if (active) AppColors.Mustard.copy(alpha = 0.15f) else AppColors.BackgroundSecondary,
        modifier = Modifier.size(100.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(Icons.Default.Star, null, modifier = Modifier.size(48.dp), tint = if (active) AppColors.Mustard else AppColors.TextDisabled)
        }
    }
}

@Composable
private fun BenefitsSection(active: Boolean) {
    SectionCard("Beneficios Premium") {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            BenefitRow("🔒 Modo Perdido", "Activa alertas cuando tu mascota se extravíe.")
            BenefitRow("📡 Historial de escaneos", "Recibe notificaciones y ve quién escaneó la placa.")
            BenefitRow("✏️ Edición ilimitada", "Edita el perfil de tu mascota sin restricciones.")
            BenefitRow("📸 Foto de mascota", "Personaliza la foto visible en la placa.")
        }
    }
}

@Composable
private fun BenefitRow(title: String, description: String) {
    Column {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Text(description, style = MaterialTheme.typography.bodySmall, color = AppColors.TextSecondary)
    }
}

@Composable
private fun SubscriptionInfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = AppColors.TextSecondary)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}
