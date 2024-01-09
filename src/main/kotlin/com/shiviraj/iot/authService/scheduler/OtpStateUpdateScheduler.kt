package com.shiviraj.iot.authService.scheduler

import com.shiviraj.iot.authService.model.OtpState
import com.shiviraj.iot.authService.repository.OtpRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class OtpStateUpdateScheduler(private val otpRepository: OtpRepository) {
    @Scheduled(cron = "0 * * * * *")
    fun start() {
        otpRepository.findAllByStateAndCreatedAtBefore(OtpState.GENERATED, LocalDateTime.now().minusMinutes(10))
            .flatMap {
                otpRepository.save(it.setExpired())
            }
            .blockLast()
    }
}
