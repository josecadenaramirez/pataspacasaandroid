package com.cadena.pataspacasa

import com.cadena.pataspacasa.domain.entity.*
import com.cadena.pataspacasa.domain.repository.PetRepository
import com.cadena.pataspacasa.domain.usecase.pet.GetPetDetailUseCase
import com.cadena.pataspacasa.domain.usecase.pet.UpdatePetInfoUseCase
import com.cadena.pataspacasa.presentation.pet.EditPetSaveState
import com.cadena.pataspacasa.presentation.pet.EditPetViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
class EditPetViewModelTest {
    private val dispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: PetRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        repository = mockk(relaxed = true)
    }

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `loads bundled catalog and preserves a removed breed`() = runTest {
        val original = pet(breed = "Raza retirada")
        val viewModel = viewModel(original)

        viewModel.setPetId(original.id)
        viewModel.onNameChange("  Sol  ")
        viewModel.save {}

        assertEquals("Mestizo / Mixto", viewModel.breedOptions.value.first())
        assertEquals("Raza retirada", viewModel.breed.value)
        coVerify(exactly = 1) {
            repository.updatePet(match { it.name == "Sol" && it.breed == "Raza retirada" })
        }
    }

    @Test
    fun `changing species clears the incompatible breed`() = runTest {
        val viewModel = viewModel(pet())
        viewModel.setPetId("pet-1")

        viewModel.onSpeciesChange(PetSpecies.CAT)
        viewModel.onBreedChange("Labrador Retriever")
        viewModel.save {}

        assertEquals("", viewModel.breed.value)
        assertEquals("Selecciona una raza.", viewModel.breedError.value)
        coVerify(exactly = 0) { repository.updatePet(any()) }
    }

    @Test
    fun `invalid visible fields never call update`() = runTest {
        val viewModel = viewModel(pet())
        viewModel.setPetId("pet-1")

        viewModel.onNameChange("   ")
        viewModel.save {}
        assertEquals("El nombre no puede estar vacío.", viewModel.nameError.value)

        viewModel.onNameChange("Luna")
        viewModel.onPhonePrimaryChange("123")
        viewModel.save {}
        assertEquals("Ingresa un número de celular válido.", viewModel.phonesError.value)

        coVerify(exactly = 0) { repository.updatePet(any()) }
    }

    @Test
    fun `saving preserves the original birth date`() = runTest {
        val birthDate = PetCalendarDate(2020, 2, 29)
        val viewModel = viewModel(pet(approxBirthDate = birthDate))
        viewModel.setPetId("pet-1")

        viewModel.onOwnerNameChange(" Ana ")
        viewModel.save {}

        coVerify(exactly = 1) {
            repository.updatePet(match { it.ownerName == "Ana" && it.approxBirthDate == birthDate })
        }
    }

    @Test
    fun `duplicate save is ignored while request is running`() = runTest {
        val gate = CompletableDeferred<Unit>()
        val original = pet()
        coEvery { repository.getPet(original.id) } returns original
        coEvery { repository.updatePet(any()) } coAnswers {
            gate.await()
            firstArg()
        }
        val viewModel = newViewModel()
        viewModel.setPetId(original.id)

        viewModel.save {}
        viewModel.save {}

        assertEquals(EditPetSaveState.SAVING, viewModel.saveState.value)
        coVerify(exactly = 1) { repository.updatePet(any()) }
        gate.complete(Unit)
    }

    @Test
    fun `failed save keeps entered values`() = runTest {
        val original = pet()
        coEvery { repository.getPet(original.id) } returns original
        coEvery { repository.updatePet(any()) } throws IllegalStateException("offline")
        val viewModel = newViewModel()
        viewModel.setPetId(original.id)
        viewModel.onNameChange("Nuevo nombre")

        viewModel.save {}

        assertEquals("Nuevo nombre", viewModel.name.value)
        assertEquals(EditPetSaveState.IDLE, viewModel.saveState.value)
        assertFalse(viewModel.saveError.value.isNullOrBlank())
    }

    private fun viewModel(pet: Pet): EditPetViewModel {
        coEvery { repository.getPet(pet.id) } returns pet
        coEvery { repository.updatePet(any()) } answers { firstArg() }
        return newViewModel()
    }

    private fun newViewModel() = EditPetViewModel(
        GetPetDetailUseCase(repository),
        UpdatePetInfoUseCase(repository)
    )

    private fun pet(
        breed: String = "Labrador Retriever",
        approxBirthDate: PetCalendarDate? = null
    ) = Pet(
        id = "pet-1", publicId = null, ownerId = "owner-1", name = "Luna",
        species = PetSpecies.DOG, breed = breed, petType = "dog",
        customPetType = null, customBreed = null, approxBirthDate = approxBirthDate,
        sex = null, sterilized = null, age = null, photoUrl = null, notes = null,
        allergies = null, treatment = null, ownerName = null, ownerEmail = null,
        phones = emptyList(), address = null, status = PetStatus.NORMAL,
        tagId = null, activationStatus = null, nfcProgrammed = null,
        nfcProgrammedAt = null, plan = PlanType.BASIC, isPremiumActive = false,
        paidUntil = null, createdAt = Date(), updatedAt = Date()
    )
}
