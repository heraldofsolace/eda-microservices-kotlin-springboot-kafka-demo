package com.example.notificationservice.consumer

import com.example.notificationservice.event.OrderCreatedEvent
import com.example.notificationservice.model.SendNotificationRequest
import com.example.notificationservice.service.NotificationService
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.stereotype.Component

@Component
class OrderEventConsumer(private val notificationService: NotificationService) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @KafkaListener(
        topics = ["orders.created"],
    )
    fun handleOrderCreated(
        event: OrderCreatedEvent,
        @Header(KafkaHeaders.RECEIVED_PARTITION) partition: Int,
        @Header(KafkaHeaders.OFFSET) offset: Long,
    ) {
        logger.info(
            "Consuming OrderCreatedEvent for order ${event.orderId} " +
                "[partition=$partition, offset=$offset]"
        )

        notificationService.sendNotification(
            SendNotificationRequest(
                customerId = event.customerId,
                subject = "Order Received",
                message = "Your order ${event.orderId} for ${event.quantity}x ${event.productId} " +
                    "has been received and is being processed.",
            )
        )
    }
}
