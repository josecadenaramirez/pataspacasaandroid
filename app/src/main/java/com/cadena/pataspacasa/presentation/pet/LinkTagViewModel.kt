package com.cadena.pataspacasa.presentation.pet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cadena.pataspacasa.core.error.AppError
import com.cadena.pataspacasa.core.error.AppErrorMapper
import com.cadena.pataspacasa.domain.entity.Pet
import com.cadena.pataspacasa.domain.usecase.pet.ClaimPetUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class LinkTagStep {
    data class Entry(val prefilled: String) : LinkTagStep()
    object Claiming : LinkTagStep()
    data class Success(val pet: Pet) : LinkTagStep()
    data class Failure(val message: String) : LinkTagStep()
}

@HiltViewModel
class LinkTagViewModel @Inject constructor(
    private val claimPetUseCase: ClaimPetUseCase
) : ViewModel() {

    private val _step = MutableStateFlow<LinkTagStep>(LinkTagStep.Entry(""))
    val step: StateFlow<LinkTagStep> = _step.asStateFlow()

    private val _tagInput = MutableStateFlow("")
    val tagInput: StateFlow<String> = _tagInput.asStateFlow()

    fun setInitialTagCode(code: String) {
        _tagInput.value = code
        _step.value = LinkTagStep.Entry(code)
    }

    fun onTagInputChange(v: String) { _tagInput.value = v }

    fun claimPet() {
        val input = _tagInput.value.trim()
        if (input.isEmpty()) return

        // Extract tag code from URL if needed: https://pataspacasa.com/p/ABC123
        val tagCode = extractTagCode(input)

        _step.value = LinkTagStep.Claiming
        viewModelScope.launch {
            try {
                val pet = claimPetUseCase.execute(tagCode)
                _step.value = LinkTagStep.Success(pet)
            } catch (e: AppError) {
                _step.value = LinkTagStep.Failure(e.userMessage)
            } catch (e: Exception) {
                _step.value = LinkTagStep.Failure(AppErrorMapper.from(e).userMessage)
            }
        }
    }

    fun retry() { _step.value = LinkTagStep.Entry(_tagInput.value) }

    private fun extractTagCode(input: String): String {
        // Try to extract from URL patterns:
        // https://pataspacasa.com/p/ABC123
        // https://pataspacasa.com/scan/ABC123
        val urlPattern = Regex("pataspacasa\\.com/(?:p|scan)/([A-Za-z0-9]+)")
        val match = urlPattern.find(input)
        return match?.groupValues?.getOrNull(1) ?: input
    }
}
