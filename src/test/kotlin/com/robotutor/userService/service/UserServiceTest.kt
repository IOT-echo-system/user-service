package com.robotutor.userService.service

import com.robotutor.iot.exceptions.BadDataException
import com.robotutor.iot.exceptions.DataNotFoundException
import com.robotutor.iot.service.IdGeneratorService
import com.robotutor.iot.services.MqttPublisher
import com.robotutor.iot.utils.assertErrorWith
import com.robotutor.iot.utils.assertNextWith
import com.robotutor.userService.builder.UserDetailsBuilder
import com.robotutor.userService.controllers.view.UserRegistrationRequest
import com.robotutor.userService.exceptions.IOTError
import com.robotutor.userService.gateway.AuthServiceGateway
import com.robotutor.userService.models.IdType
import com.robotutor.userService.repositories.UserRepository
import com.robotutor.userService.services.UserService
import io.kotest.matchers.shouldBe
import io.mockk.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import java.time.ZoneId

class UserServiceTest {
    private val userRepository = mockk<UserRepository>()
    private val idGeneratorService = mockk<IdGeneratorService>()
    private val authServiceGateway = mockk<AuthServiceGateway>()
    private val mqttPublisher = mockk<MqttPublisher>()

    private val userService = UserService(
        userRepository = userRepository,
        idGeneratorService = idGeneratorService,
        authServiceGateway = authServiceGateway
    )
    private val mockTime = LocalDateTime.of(2024, 1, 1, 1, 1)

    @BeforeEach
    fun setUp() {
        clearAllMocks()
        mockkStatic(LocalDateTime::class)
        every { LocalDateTime.now(ZoneId.of("UTC")) } returns mockTime
        every { mqttPublisher.publish(any(), any()) } just Runs
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `should not register a new user if already registered`() {
        val userDetails = UserRegistrationRequest(name = "name", email = "email", password = "password")
        val user = UserDetailsBuilder().build()

        every { userRepository.findByEmail(any()) } returns Mono.just(user)

        val response = userService.register(userDetails)

        assertErrorWith(response) {
            it shouldBe BadDataException(IOTError.IOT0201)
            verify(exactly = 1) {
                userRepository.findByEmail("email")
            }
            verify(exactly = 0) {
                userRepository.save(any())
            }
        }
    }

    @Test
    fun `should register a new user`() {
        val userDetails = UserRegistrationRequest(name = "name", email = "email", password = "password")
        val user = UserDetailsBuilder(name = "name", email = "email", userId = "001", registeredAt = mockTime).build()

        every { LocalDateTime.now() } returns mockTime
        every { userRepository.findByEmail(any()) } returns Mono.empty()
        every { userRepository.save(any()) } returns Mono.just(user)
        every { idGeneratorService.generateId(any()) } returns Mono.just("001")
        every { authServiceGateway.saveUserPassword(any(), any()) } returns Mono.just(true)

        val response = userService.register(userDetails)
            .contextWrite { it.put(MqttPublisher::class.java, mqttPublisher) }

        assertNextWith(response) {
            it shouldBe user
            verify(exactly = 1) {
                idGeneratorService.generateId(IdType.USER_ID)
                userRepository.findByEmail("email")
                userRepository.save(user)
                authServiceGateway.saveUserPassword("001", "password")
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
            it shouldBe DataNotFoundException(IOTError.IOT0202)

            verify(exactly = 1) {
                userRepository.findByEmail("example@email.com")
            }
        }
    }
}
