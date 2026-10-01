package com.cadena.pataspacasa

import com.cadena.pataspacasa.core.error.AppError
import com.cadena.pataspacasa.domain.entity.*
import com.cadena.pataspacasa.domain.usecase.auth.LoginUseCase
import com.cadena.pataspacasa.domain.usecase.auth.RegisterUseCase
import com.cadena.pataspacasa.domain.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import java.util.Date

class AuthValidationTest {

    private val authRepository = mockk<AuthRepository>()
    private val loginUseCase = LoginUseCase(authRepository)
    private val registerUseCase = RegisterUseCase(authRepository)

    private val mockUser = User(
        id = "uid1",
        name = "Test User",
        email = "test@example.com",
        photoUrl = null,
        plan = PlanType.BASIC,
        createdAt = Date()
    )

    @Test
    fun `login fails with empty email`() = runTest {
        val exception = runCatching { loginUseCase.execute("", "password123") }.exceptionOrNull()
        assertNotNull(exception)
        assertTrue(exception is IllegalArgumentException)
    }

    @Test
    fun `login fails with empty password`() = runTest {
        val exception = runCatching { loginUseCase.execute("test@test.com", "") }.exceptionOrNull()
        assertNotNull(exception)
        assertTrue(exception is IllegalArgumentException)
    }

    @Test
    fun `login succeeds with valid credentials`() = runTest {
        coEvery { authRepository.signIn("test@example.com", "password123") } returns mockUser
        val user = loginUseCase.execute("test@example.com", "password123")
        assertEquals("uid1", user.id)
    }

    @Test
    fun `register fails with short name`() = runTest {
        val exception = runCatching { registerUseCase.execute("A", "test@test.com", "Password123") }.exceptionOrNull()
        assertNotNull(exception)
    }

    @Test
    fun `register fails with short password`() = runTest {
        val exception = runCatching { registerUseCase.execute("Test User", "test@test.com", "abc") }.exceptionOrNull()
        assertNotNull(exception)
    }

    @Test
    fun `register fails with no uppercase in password`() = runTest {
        val exception = runCatching { registerUseCase.execute("Test User", "test@test.com", "password123") }.exceptionOrNull()
        assertNotNull(exception)
    }

    @Test
    fun `register succeeds with valid data`() = runTest {
        coEvery { authRepository.register("Test User", "test@example.com", "Password123") } returns mockUser
        val user = registerUseCase.execute("Test User", "test@example.com", "Password123")
        assertEquals("uid1", user.id)
    }
}
