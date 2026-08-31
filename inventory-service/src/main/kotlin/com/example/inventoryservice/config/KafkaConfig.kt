package com.example.inventoryservice.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.listener.CommonErrorHandler
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer
import org.springframework.kafka.listener.DefaultErrorHandler
import org.springframework.util.backoff.FixedBackOff

@Configuration
class KafkaConfig {
    @Bean
    fun commonErrorHandler(kafkaTemplate: KafkaTemplate<Any, Any>): CommonErrorHandler {
        val recoverer = DeadLetterPublishingRecoverer(kafkaTemplate)
        return DefaultErrorHandler(recoverer, FixedBackOff(1_000L, 3L))
    }
}
