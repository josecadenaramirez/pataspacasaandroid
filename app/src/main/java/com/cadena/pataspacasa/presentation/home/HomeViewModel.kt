package com.cadena.pataspacasa.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cadena.pataspacasa.core.error.AppError
import com.cadena.pataspacasa.core.error.AppErrorMapper
import com.cadena.pataspacasa.domain.entity.Pet
import com.cadena.pataspacasa.domain.repository.AuthRepository
import com.cadena.pataspacasa.domain.usecase.pet.GetPetsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Success(val pets: List<Pet>) : HomeUiState()
    object Empty : HomeUiState()
    data class Error(val message: String) : HomeUiState()
    object Offline : HomeUiState()
    object Unauthorized : HomeUiState()
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getPetsUseCase: GetPetsUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _userName = MutableStateFlow("")
    val userName: StateFlow<String> = _userName.asStateFlow()

    val firstName: String
        get() = _userName.value.split(" ").firstOrNull() ?: _userName.value

    init {
        loadData()
    }

    fun loadData() {
        resolveUserName()
        fetchPets()
    }

    fun refresh() = fetchPets()
    fun retry() = loadData()

    private fun resolveUserName() {
        _userName.value = authRepository.currentUser()?.name ?: ""
    }

    private fun fetchPets() {
        _uiState.value = HomeUiState.Loading
        viewModelScope.launch {
            try {
                val pets = getPetsUseCase.execute()
                _uiState.value = if (pets.isEmpty()) HomeUiState.Empty else HomeUiState.Success(pets)
            } catch (e: AppError) {
                _uiState.value = when (e) {
                    is AppError.NotAuthenticated, is AppError.SessionExpired -> HomeUiState.Unauthorized
                    is AppError.NetworkUnavailable -> HomeUiState.Offline
                    else -> HomeUiState.Error(e.userMessage)
                }
            } catch (e: Exception) {
                val appError = AppErrorMapper.from(e)
                _uiState.value = when (appError) {
                    is AppError.NotAuthenticated, is AppError.SessionExpired -> HomeUiState.Unauthorized
                    is AppError.NetworkUnavailable -> HomeUiState.Offline
                    else -> HomeUiState.Error(appError.userMessage)
                }
            }
        }
    }
}
