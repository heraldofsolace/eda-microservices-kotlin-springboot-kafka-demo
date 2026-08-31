package com.example.orderservice.config

import org.apache.kafka.clients.admin.NewTopic
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.TopicBuilder

@Configuration
class KafkaTopicConfig {

    @Bean
    fun ordersCreatedTopic(): NewTopic =
        TopicBuilder.name("orders.created")
            .partitions(3)
            .replicas(1)
            .build()

    @Bean
    fun ordersCreatedDlt(): NewTopic =
        TopicBuilder.name("orders.created-dlt")
            .partitions(3)
            .replicas(1)
            .build()
}
