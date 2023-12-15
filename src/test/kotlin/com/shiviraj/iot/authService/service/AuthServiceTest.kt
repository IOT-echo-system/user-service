package com.shiviraj.iot.authService.service

import com.shiviraj.iot.authService.builder.UserDetailsBuilder
import com.shiviraj.iot.authService.controller.view.UserLoginDetails
import com.shiviraj.iot.authService.controller.view.UserSignUpDetails
import com.shiviraj.iot.authService.exception.IOTError
import com.shiviraj.iot.authService.model.IdType
import com.shiviraj.iot.authService.repository.AuthRepository
import com.shiviraj.iot.authService.testUtils.assertErrorWith
import com.shiviraj.iot.authService.testUtils.assertNextWith
import com.shiviraj.iot.userService.exceptions.BadDataException
import com.shiviraj.iot.utils.service.IdGeneratorService
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.security.crypto.password.PasswordEncoder
import reactor.core.publisher.Mono

class AuthServiceTest {
    private val authRepository = mockk<AuthRepository>()
    private val idGeneratorService = mockk<IdGeneratorService>()
    private val passwordEncoder = mockk<PasswordEncoder>()
    private val authService = AuthService(
        authRepository = authRepository,
        idGeneratorService = idGeneratorService,
        passwordEncoder = passwordEncoder
    )

    @BeforeEach
    fun setUp() {
        clearAllMocks()
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `should not register a new user`() {
        val userDetails = UserSignUpDetails(name = "name", email = "email", password = "password")

        every { authRepository.existsByEmail(any()) } returns Mono.just(true)

        val response = authService.register(userDetails)

        assertErrorWith(response) {
            it shouldBe BadDataException(IOTError.IOT0101)
            verify(exactly = 1) {
                authRepository.existsByEmail("email")
            }
            verify(exactly = 0) {
                authRepository.save(any())
            }
        }
    }

    @Test
    fun `should register a new user`() {
        val userDetails = UserSignUpDetails(name = "name", email = "email", password = "password")
        val user = UserDetailsBuilder().build()

        every { authRepository.existsByEmail(any()) } returns Mono.just(false)
        every { authRepository.save(any()) } returns Mono.just(user)
        every { passwordEncoder.encode(any()) } returns "encodedPassword"
        every { idGeneratorService.generateId(any()) } returns Mono.just("001")

        val response = authService.register(userDetails)

        assertNextWith(response) {
            it shouldBe user
            verify(exactly = 1) {
                passwordEncoder.encode("password")
                idGeneratorService.generateId(IdType.USER_ID)
                authRepository.existsByEmail("email")
                authRepository.save(
                    UserDetailsBuilder(
                        userId = "001",
                        password = "encodedPassword",
                        email = "email",
                        name = "name"
                    ).build()
                )
            }
        }
    }

    @Test
    fun `should verify credentials`() {
        val userDetails = UserLoginDetails(email = "email", password = "password")
        val user = UserDetailsBuilder(email = "email", password = "encodedPassword").build()

        every { authRepository.findByEmail(any()) } returns Mono.just(user)
        every { passwordEncoder.matches(any(), any()) } returns true

        val response = authService.verifyCredentials(userDetails)

        assertNextWith(response) {
            it shouldBe user
            verify(exactly = 1) {
                passwordEncoder.matches("password", "encodedPassword")
                authRepository.findByEmail("email")
            }
        }
    }

    @Test
    fun `should give mono error if invalid email while verifying credentials`() {
        val userDetails = UserLoginDetails(email = "email", password = "password")

        every { authRepository.findByEmail(any()) } returns Mono.empty()

        val response = authService.verifyCredentials(userDetails)

        assertErrorWith(response) {
            it shouldBe BadDataException(IOTError.IOT0102)
            verify(exactly = 1) {
                authRepository.findByEmail("email")
            }
        }
    }

    @Test
    fun `should give mono error if invalid password while verifying credentials`() {
        val userDetails = UserLoginDetails(email = "email", password = "password")
        val user = UserDetailsBuilder(email = "email", password = "encodedPassword").build()

        every { authRepository.findByEmail(any()) } returns Mono.just(user)
        every { passwordEncoder.matches(any(), any()) } returns false

        val response = authService.verifyCredentials(userDetails)

        assertErrorWith(response) {
            it shouldBe BadDataException(IOTError.IOT0102)
            verify(exactly = 1) {
                authRepository.findByEmail("email")
                passwordEncoder.matches("password", "encodedPassword")
            }
        }
    }
}
