package com.example.inventoryservice.config

import com.example.inventoryservice.event.OrderCreatedEvent
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer
import org.springframework.kafka.listener.DefaultErrorHandler
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer
import org.springframework.kafka.support.serializer.JacksonJsonSerializer
import org.springframework.util.backoff.FixedBackOff

@Configuration
class KafkaConfig(
    @param:Value("\${spring.kafka.bootstrap-servers}") private val bootstrapServers: String,
    @param:Value("\${spring.kafka.consumer.group-id}") private val groupId: String,
) {
    @Bean
    fun consumerFactory(): ConsumerFactory<String, OrderCreatedEvent> {
        val jsonDeserializer = JacksonJsonDeserializer(OrderCreatedEvent::class.java)
            .trustedPackages("com.example.*")
            .ignoreTypeHeaders()
        val config = mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ConsumerConfig.GROUP_ID_CONFIG to groupId,
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest",
        )
        return DefaultKafkaConsumerFactory(config, StringDeserializer(), jsonDeserializer)
    }

    @Bean
    fun kafkaListenerContainerFactory(
        consumerFactory: ConsumerFactory<String, OrderCreatedEvent>,
        kafkaTemplate: KafkaTemplate<String, OrderCreatedEvent>,
    ): ConcurrentKafkaListenerContainerFactory<String, OrderCreatedEvent> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, OrderCreatedEvent>()
        factory.setConsumerFactory(consumerFactory)

        // Process up to 3 partitions concurrently (match the number of topic partitions)
        factory.setConcurrency(3)

        // On failure: retry up to 3 times with a 1-second pause, then forward to the DLT
        val recoverer = DeadLetterPublishingRecoverer(kafkaTemplate)
        factory.setCommonErrorHandler(DefaultErrorHandler(recoverer, FixedBackOff(1_000L, 3)))

        return factory
    }

    @Bean
    fun producerFactory(): ProducerFactory<String, OrderCreatedEvent> {
        val config = mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to JacksonJsonSerializer::class.java,
        )
        return DefaultKafkaProducerFactory(config)
    }

    @Bean
    fun kafkaTemplate(
        producerFactory: ProducerFactory<String, OrderCreatedEvent>,
    ): KafkaTemplate<String, OrderCreatedEvent> = KafkaTemplate(producerFactory)
}
