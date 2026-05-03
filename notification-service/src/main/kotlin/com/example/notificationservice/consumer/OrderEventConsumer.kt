package com.example.notificationservice.consumer

import com.example.notificationservice.event.OrderCreatedEvent
import com.example.notificationservice.model.SendNotificationRequest
import com.example.notificationservice.service.NotificationService
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class OrderEventConsumer(
    private val notificationService: NotificationService,
    private val objectMapper: ObjectMapper,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @KafkaListener(topics = ["orders.created"])
    fun handleOrderCreated(payload: String) {
        val event = objectMapper.readValue(payload, OrderCreatedEvent::class.java)
        logger.info("Received OrderCreatedEvent for order ${event.orderId}")

        notificationService.sendNotification(
            SendNotificationRequest(
                customerId = event.customerId,
                subject = "Order Confirmed",
                message = "Your order ${event.orderId} for ${event.quantity}x ${event.productId} has been confirmed.",
            )
        )
    }
}
