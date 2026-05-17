package com.solari.app.ui.viewmodels

import app.cash.turbine.test
import com.solari.app.data.auth.AuthRepository
import com.solari.app.data.network.ApiResult
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SignInViewModelTest {

    private val authRepository = mockk<AuthRepository>()
    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: SignInViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = SignInViewModel(authRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `signIn success updates state to isSignedIn`() = runTest {
        viewModel.onEmailOrUsernameChanged("testuser")
        viewModel.onPasswordChanged("password")

        coEvery { authRepository.signIn("testuser", "password") } returns ApiResult.Success(mockk())

        viewModel.uiState.test {
            val initialState = awaitItem()
            assertEquals("testuser", initialState.emailOrUsername)
            assertEquals("password", initialState.password)
            assertFalse(initialState.isLoading)

            viewModel.signIn()

            val loadingState = awaitItem()
            assertTrue(loadingState.isLoading)
            assertNull(loadingState.errorMessage)

            val successState = awaitItem()
            assertFalse(successState.isLoading)
            assertTrue(successState.isSignedIn)
            assertEquals("", successState.password)
        }
    }

    @Test
    fun `signIn failure updates state with error message`() = runTest {
        viewModel.onEmailOrUsernameChanged("testuser")
        viewModel.onPasswordChanged("wrongpassword")

        coEvery { authRepository.signIn("testuser", "wrongpassword") } returns ApiResult.Failure(null, null, "Invalid credentials")

        viewModel.uiState.test {
            awaitItem() // initial

            viewModel.signIn()

            awaitItem() // loading

            val errorState = awaitItem()
            assertFalse(errorState.isLoading)
            assertEquals("Invalid credentials", errorState.errorMessage)
            assertFalse(errorState.isSignedIn)
        }
    }

    @Test
    fun `consumeSignedIn resets isSignedIn state`() = runTest {
        // Prepare state where signed in is true
        coEvery { authRepository.signIn(any(), any()) } returns ApiResult.Success(mockk())
        viewModel.signIn()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isSignedIn)

        viewModel.consumeSignedIn()

        assertFalse(viewModel.uiState.value.isSignedIn)
    }
}
