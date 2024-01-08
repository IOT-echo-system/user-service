package com.shiviraj.iot.authService.service

import com.shiviraj.iot.authService.builder.UserDetailsBuilder
import com.shiviraj.iot.authService.controller.view.UserLoginRequest
import com.shiviraj.iot.authService.controller.view.UserSignUpRequest
import com.shiviraj.iot.authService.exception.IOTError
import com.shiviraj.iot.authService.model.IdType
import com.shiviraj.iot.authService.repository.UserRepository
import com.shiviraj.iot.authService.testUtils.assertErrorWith
import com.shiviraj.iot.authService.testUtils.assertNextWith
import com.shiviraj.iot.userService.exceptions.BadDataException
import com.shiviraj.iot.userService.exceptions.DataNotFoundException
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

class UserServiceTest {
    private val userRepository = mockk<UserRepository>()
    private val idGeneratorService = mockk<IdGeneratorService>()
    private val passwordEncoder = mockk<PasswordEncoder>()
    private val userService = UserService(
        userRepository = userRepository,
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
        val user = UserDetailsBuilder().build()

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
                userRepository.save(
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
        val userDetails = UserLoginRequest(email = "email", password = "password")
        val user = UserDetailsBuilder(email = "email", password = "encodedPassword").build()

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
        val user = UserDetailsBuilder(email = "email", password = "encodedPassword").build()

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

        assertNextWith(response){
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

        assertErrorWith(response){
            it shouldBe DataNotFoundException(IOTError.IOT0106)

            verify(exactly = 1) {
                userRepository.findByEmail("example@email.com")
            }
        }
    }
}
