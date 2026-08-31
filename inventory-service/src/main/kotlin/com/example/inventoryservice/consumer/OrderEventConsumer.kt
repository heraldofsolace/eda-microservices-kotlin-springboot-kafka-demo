package com.example.inventoryservice.consumer

import com.example.inventoryservice.event.OrderCreatedEvent
import com.example.inventoryservice.service.InventoryService
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.stereotype.Component

@Component
class OrderEventConsumer(private val inventoryService: InventoryService) {
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

        val (success, remaining) = inventoryService.reserveStock(event.productId, event.quantity)

        if (success) {
            logger.info(
                "Reserved ${event.quantity} units of ${event.productId} " +
                    "for order ${event.orderId}. Remaining: $remaining"
            )
        } else {
            logger.warn("Insufficient stock for order ${event.orderId}; throwing to trigger retry")
            error("Insufficient stock for product ${event.productId}")
        }
    }
}
