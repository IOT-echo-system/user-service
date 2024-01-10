package com.shiviraj.iot.authService.scheduler

import com.shiviraj.iot.authService.builder.OtpBuilder
import com.shiviraj.iot.authService.model.OtpState
import com.shiviraj.iot.authService.repository.OtpRepository
import io.mockk.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime

class OtpStateUpdateSchedulerTest {
    private val otpRepository = mockk<OtpRepository>()
    private val otpStateUpdateScheduler = OtpStateUpdateScheduler(otpRepository = otpRepository)

    @BeforeEach
    fun setUp() {
        clearAllMocks()
        mockkStatic(LocalDateTime::class)
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `should set all otp as expired if older than 10 min`() {
        val before10Min = LocalDateTime.of(2023, 1, 1, 1, 0)
        val now = LocalDateTime.of(2023, 1, 1, 1, 10)
        val otp = OtpBuilder(otpId = "otpId", value = "value", email = "example@email.com", createdAt = now).build()

        every { LocalDateTime.now() } returns now
        every { otpRepository.findAllByStateAndCreatedAtBefore(any(), any()) } returns Flux.just(otp)
        every { otpRepository.save(any()) } returns Mono.just(otp)

        otpStateUpdateScheduler.start()

        verify(exactly = 1) {
            otpRepository.findAllByStateAndCreatedAtBefore(OtpState.GENERATED,before10Min)
            otpRepository.save(otp.copy(state = OtpState.EXPIRED))
        }
    }
}
