package com.example.orderservice.config

import com.example.orderservice.event.OrderCreatedEvent
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.support.serializer.JacksonJsonSerializer

@Configuration
class KafkaConfig(
    @param:Value("\${spring.kafka.bootstrap-servers}") private val bootstrapServers: String,
) {
    @Bean
    fun producerFactory(): ProducerFactory<String, OrderCreatedEvent> {
        val config = mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to JacksonJsonSerializer::class.java,
            // Guarantee at-least-once delivery: wait for all in-sync replicas to acknowledge
            ProducerConfig.ACKS_CONFIG to "all",
            // Enable idempotent producer to prevent duplicate messages on retry
            ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG to true,
            ProducerConfig.RETRIES_CONFIG to 3,
        )
        return DefaultKafkaProducerFactory(config)
    }

    @Bean
    fun kafkaTemplate(producerFactory: ProducerFactory<String, OrderCreatedEvent>): KafkaTemplate<String, OrderCreatedEvent> =
        KafkaTemplate(producerFactory)
}
