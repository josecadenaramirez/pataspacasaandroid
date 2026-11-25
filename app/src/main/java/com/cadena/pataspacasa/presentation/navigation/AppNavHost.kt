package com.cadena.pataspacasa.presentation.navigation

import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.cadena.pataspacasa.presentation.auth.ForgotPasswordScreen
import com.cadena.pataspacasa.presentation.auth.LoginScreen
import com.cadena.pataspacasa.presentation.auth.RegisterScreen
import com.cadena.pataspacasa.presentation.auth.SplashScreen
import com.cadena.pataspacasa.presentation.home.HomeScreen
import com.cadena.pataspacasa.presentation.pet.AddPetScreen
import com.cadena.pataspacasa.presentation.pet.EditPetScreen
import com.cadena.pataspacasa.presentation.pet.LinkTagScreen
import com.cadena.pataspacasa.presentation.pet.PetDetailScreen
import com.cadena.pataspacasa.presentation.pet.lostmode.LostModeScreen
import com.cadena.pataspacasa.presentation.pet.lostmode.LostPetScanDetailScreen
import com.cadena.pataspacasa.presentation.pet.lostmode.LostPetScanHistoryScreen
import com.cadena.pataspacasa.presentation.user.ProfileScreen
import androidx.hilt.navigation.compose.hiltViewModel
import com.cadena.pataspacasa.domain.repository.SessionState

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    startDestination: Any = SplashRoute,
    pendingLaunchIntent: LaunchIntent? = null,
    onLaunchIntentConsumed: () -> Unit = {}
) {
    val sessionViewModel: SessionViewModel = hiltViewModel()
    val sessionState by sessionViewModel.state.collectAsState()
    var authenticatedUserId by remember { mutableStateOf<String?>(null) }
    var launchIntent by remember { mutableStateOf<LaunchIntent?>(null) }
    LaunchedEffect(pendingLaunchIntent) {
        if (pendingLaunchIntent != null) {
            launchIntent = pendingLaunchIntent
        }
    }
    val currentEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentEntry?.destination?.route.orEmpty()

    LaunchedEffect(sessionState) {
        when (val session = sessionState) {
            is SessionState.Authenticated -> authenticatedUserId = session.user.id
            SessionState.Unauthenticated -> if (authenticatedUserId != null) {
                authenticatedUserId = null
                launchIntent = null
                onLaunchIntentConsumed()
                navController.navigate(LoginRoute) {
                    popUpTo(navController.graph.id) { inclusive = true }
                    launchSingleTop = true
                }
            }
            null -> Unit
        }
    }

    fun consumePendingAfterAuth() {
        val current = launchIntent ?: return
        when (current) {
            is LaunchIntent.LinkTag -> navController.navigate(LinkTagRoute(current.tagCode))
            is LaunchIntent.LostPetScan -> navController.navigate(
                LostPetScanDetailRoute(current.eventId, current.petId)
            )
            is LaunchIntent.PetDetail -> navController.navigate(PetDetailRoute(current.petId))
            LaunchIntent.Profile -> navController.navigate(ProfileRoute)
        }
        launchIntent = null
        onLaunchIntentConsumed()
    }

    LaunchedEffect(launchIntent, currentRoute) {
        if (launchIntent == null) return@LaunchedEffect
        val onAuthFlow = currentRoute.contains("LoginRoute") ||
            currentRoute.contains("RegisterRoute") ||
            currentRoute.contains("ForgotPasswordRoute") ||
            currentRoute.contains("SplashRoute")
        if (!onAuthFlow) {
            consumePendingAfterAuth()
        }
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable<SplashRoute> {
            SplashScreen(
                onAuthResolved = { isAuthenticated ->
                    if (isAuthenticated) {
                        navController.navigate(HomeRoute) {
                            popUpTo(SplashRoute) { inclusive = true }
                        }
                        consumePendingAfterAuth()
                    } else {
                        navController.navigate(LoginRoute) {
                            popUpTo(SplashRoute) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable<LoginRoute> {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(HomeRoute) {
                        popUpTo(LoginRoute) { inclusive = true }
                    }
                    consumePendingAfterAuth()
                },
                onNavigateToRegister = { navController.navigate(RegisterRoute) },
                onNavigateToForgotPassword = { email ->
                    navController.navigate(ForgotPasswordRoute(email))
                }
            )
        }

        composable<RegisterRoute> {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.popBackStack()
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<ForgotPasswordRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<ForgotPasswordRoute>()
            ForgotPasswordScreen(
                initialEmail = route.email ?: "",
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<HomeRoute> {
            HomeScreen(
                onNavigateToPetDetail = { petId -> navController.navigate(PetDetailRoute(petId)) },
                onNavigateToAddPet = { navController.navigate(AddPetRoute) },
                onNavigateToProfile = { navController.navigate(ProfileRoute) }
            )
        }

        composable<PetDetailRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<PetDetailRoute>()
            PetDetailScreen(
                petId = route.petId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEditPet = { petId -> navController.navigate(EditPetRoute(petId)) },
                onNavigateToLostMode = { petId -> navController.navigate(LostModeRoute(petId)) },
                onNavigateToLinkTag = { tagCode -> navController.navigate(LinkTagRoute(tagCode)) },
                onNavigateToScanHistory = { petId, petName ->
                    navController.navigate(LostPetScanHistoryRoute(petId, petName))
                }
            )
        }

        composable<EditPetRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<EditPetRoute>()
            EditPetScreen(
                petId = route.petId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<AddPetRoute> {
            AddPetScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPetDetail = { petId ->
                    navController.navigate(PetDetailRoute(petId)) {
                        popUpTo(AddPetRoute) { inclusive = true }
                    }
                }
            )
        }

        composable<LinkTagRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<LinkTagRoute>()
            LinkTagScreen(
                initialTagCode = route.tagCode,
                onNavigateBack = { navController.popBackStack() },
                onClaimSuccess = {
                    navController.navigate(HomeRoute) {
                        popUpTo(HomeRoute) { inclusive = false }
                    }
                }
            )
        }

        composable<LostModeRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<LostModeRoute>()
            LostModeScreen(
                petId = route.petId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<LostPetScanDetailRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<LostPetScanDetailRoute>()
            LostPetScanDetailScreen(
                eventId = route.eventId,
                petId = route.petId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<LostPetScanHistoryRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<LostPetScanHistoryRoute>()
            LostPetScanHistoryScreen(
                petId = route.petId,
                petName = route.petName,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToScanDetail = { eventId ->
                    navController.navigate(LostPetScanDetailRoute(eventId, route.petId))
                }
            )
        }

        composable<ProfileRoute> {
            ProfileScreen(
                onNavigateBack = { navController.popBackStack() },
                onLogout = {
                    navController.navigate(LoginRoute) {
                        popUpTo(HomeRoute) { inclusive = true }
                    }
                }
            )
        }
    }
}
