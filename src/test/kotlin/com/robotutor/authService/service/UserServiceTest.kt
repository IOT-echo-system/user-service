package com.robotutor.authService.service

import com.robotutor.authService.builder.UserDetailsBuilder
import com.robotutor.authService.controllers.view.UserLoginRequest
import com.robotutor.authService.controllers.view.UserSignUpRequest
import com.robotutor.authService.exceptions.IOTError
import com.robotutor.authService.models.IdType
import com.robotutor.authService.repositories.UserRepository
import com.robotutor.authService.services.UserService
import com.robotutor.authService.testUtils.assertErrorWith
import com.robotutor.authService.testUtils.assertNextWith
import com.robotutor.iot.exceptions.BadDataException
import com.robotutor.iot.exceptions.DataNotFoundException
import com.robotutor.iot.service.IdGeneratorService
import io.kotest.matchers.shouldBe
import io.mockk.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.security.crypto.password.PasswordEncoder
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import java.time.ZoneId

class UserServiceTest {
    private val userRepository = mockk<UserRepository>()
    private val idGeneratorService = mockk<IdGeneratorService>()
    private val passwordEncoder = mockk<PasswordEncoder>()
    private val userService = UserService(
        userRepository = userRepository,
        idGeneratorService = idGeneratorService,
        passwordEncoder = passwordEncoder,
    )
    private val mockTime = LocalDateTime.of(2024, 1, 1, 1, 1)

    @BeforeEach
    fun setUp() {
        clearAllMocks()
        mockkStatic(LocalDateTime::class)
        every { LocalDateTime.now(ZoneId.of("UTC")) } returns mockTime
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `should not register a new user`() {
        val userDetails = UserSignUpRequest(name = "name", email = "email", password = "password")

        every { userRepository.existsByEmail(any()) } returns Mono.just(true)

        val response = userService.register(userDetails)

        assertErrorWith(response) {
            it shouldBe BadDataException(IOTError.IOT0101)
            verify(exactly = 1) {
                userRepository.existsByEmail("email")
            }
            verify(exactly = 0) {
                userRepository.save(any())
            }
        }
    }

    @Test
    fun `should register a new user`() {
        val userDetails = UserSignUpRequest(name = "name", email = "email", password = "password")
        val user = UserDetailsBuilder(
            name = "name",
            email = "email",
            password = "encodedPassword",
            userId = "001",
            registeredAt = mockTime
        ).build()

        every { LocalDateTime.now() } returns mockTime
        every { userRepository.existsByEmail(any()) } returns Mono.just(false)
        every { userRepository.save(any()) } returns Mono.just(user)
        every { passwordEncoder.encode(any()) } returns "encodedPassword"
        every { idGeneratorService.generateId(any()) } returns Mono.just("001")

        val response = userService.register(userDetails)

        assertNextWith(response) {
            it shouldBe user
            verify(exactly = 1) {
                passwordEncoder.encode("password")
                idGeneratorService.generateId(IdType.USER_ID)
                userRepository.existsByEmail("email")
                userRepository.save(user)
            }
        }
    }

    @Test
    fun `should verify credentials`() {
        val userDetails = UserLoginRequest(email = "email", password = "password")
        val user = UserDetailsBuilder(userId = "userId", email = "email", password = "encodedPassword").build()

        every { userRepository.findByEmail(any()) } returns Mono.just(user)
        every { passwordEncoder.matches(any(), any()) } returns true

        val response = userService.verifyCredentials(userDetails)

        assertNextWith(response) {
            it shouldBe user
            verify(exactly = 1) {
                passwordEncoder.matches("password", "encodedPassword")
                userRepository.findByEmail("email")
            }
        }
    }

    @Test
    fun `should give mono error if invalid email while verifying credentials`() {
        val userDetails = UserLoginRequest(email = "email", password = "password")

        every { userRepository.findByEmail(any()) } returns Mono.empty()

        val response = userService.verifyCredentials(userDetails)

        assertErrorWith(response) {
            it shouldBe BadDataException(IOTError.IOT0102)
            verify(exactly = 1) {
                userRepository.findByEmail("email")
            }
        }
    }

    @Test
    fun `should give mono error if invalid password while verifying credentials`() {
        val userDetails = UserLoginRequest(email = "email", password = "password")
        val user = UserDetailsBuilder(userId = "userId", email = "email", password = "encodedPassword").build()

        every { userRepository.findByEmail(any()) } returns Mono.just(user)
        every { passwordEncoder.matches(any(), any()) } returns false

        val response = userService.verifyCredentials(userDetails)

        assertErrorWith(response) {
            it shouldBe BadDataException(IOTError.IOT0102)
            verify(exactly = 1) {
                userRepository.findByEmail("email")
                passwordEncoder.matches("password", "encodedPassword")
            }
        }
    }

    @Test
    fun `should get user by email`() {
        val user = UserDetailsBuilder().build()
        every { userRepository.findByEmail(any()) } returns Mono.just(user)

        val response = userService.getUserByEmail("example@email.com")

        assertNextWith(response) {
            it shouldBe user

            verify(exactly = 1) {
                userRepository.findByEmail("example@email.com")
            }
        }
    }

    @Test
    fun `should give error if user not exist by email`() {
        every { userRepository.findByEmail(any()) } returns Mono.empty()

        val response = userService.getUserByEmail("example@email.com")

        assertErrorWith(response) {
            it shouldBe DataNotFoundException(IOTError.IOT0106)

            verify(exactly = 1) {
                userRepository.findByEmail("example@email.com")
            }
        }
    }

    @Test
    fun `should reset user password`() {
        val userDetails =
            UserDetailsBuilder(userId = "userId", email = "example@email.com", password = "encodedPassword").build()
        every { userRepository.findByUserId(any()) } returns Mono.just(userDetails)
        every { userRepository.findByEmail(any()) } returns Mono.just(userDetails)
        every { passwordEncoder.matches(any(), any()) } returns true
        every { userRepository.save(any()) } returns Mono.just(userDetails)
        every { passwordEncoder.encode(any()) } returns "newEncodedPassword"

        val response = userService.resetPassword("userId", currentPassword = "password", password = "new password")

        assertNextWith(response) {
            it shouldBe userDetails

            verify(exactly = 1) {
                userRepository.findByUserId("userId")
                userRepository.findByEmail("example@email.com")
                passwordEncoder.matches("password", "encodedPassword")
                passwordEncoder.encode("new password")
                userRepository.save(userDetails.copy(password = "newEncodedPassword"))
            }
        }
    }

    @Test
    fun `should not reset user password if current password is not valid`() {
        val userDetails =
            UserDetailsBuilder(userId = "userId", email = "example@email.com", password = "encodedPassword").build()
        every { userRepository.findByUserId(any()) } returns Mono.just(userDetails)
        every { userRepository.findByEmail(any()) } returns Mono.just(userDetails)
        every { passwordEncoder.matches(any(), any()) } returns false

        val response = userService.resetPassword("userId", password = "new password", currentPassword = "")

        assertErrorWith(response) {
            it shouldBe BadDataException(IOTError.IOT0102)

            verify(exactly = 1) {
                userRepository.findByUserId("userId")
                userRepository.findByEmail("example@email.com")
                passwordEncoder.matches("", "encodedPassword")
            }
        }
    }

    @Test
    fun `should reset user password with otp`() {
        val userDetails =
            UserDetailsBuilder(userId = "userId", email = "example@email.com", password = "encodedPassword").build()
        every { userRepository.findByUserId(any()) } returns Mono.just(userDetails)
        every { userRepository.save(any()) } returns Mono.just(userDetails)
        every { passwordEncoder.encode(any()) } returns "encodedPassword"

        val response = userService.resetPassword("userId", password = "password")

        assertNextWith(response) {
            it shouldBe userDetails

            verify(exactly = 1) {
                userRepository.findByUserId("userId")
                passwordEncoder.encode("password")
                userRepository.save(userDetails.copy(password = "encodedPassword"))
            }
        }
    }
}
