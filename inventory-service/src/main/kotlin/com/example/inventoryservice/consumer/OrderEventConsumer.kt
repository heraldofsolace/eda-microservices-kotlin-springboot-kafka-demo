package com.example.inventoryservice.consumer

import com.example.inventoryservice.event.OrderCreatedEvent
import com.example.inventoryservice.service.InventoryService
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class OrderEventConsumer(
    private val inventoryService: InventoryService,
    private val objectMapper: ObjectMapper,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @KafkaListener(topics = ["orders.created"])
    fun handleOrderCreated(payload: String) {
        val event = objectMapper.readValue(payload, OrderCreatedEvent::class.java)
        logger.info("Received OrderCreatedEvent for order ${event.orderId}")

        val (success, remaining) = inventoryService.reserveStock(event.productId, event.quantity)

        if (success) {
            logger.info(
                "Reserved ${event.quantity} units of ${event.productId} " +
                    "for order ${event.orderId}. Remaining: $remaining"
            )
        } else {
            logger.warn("Could not reserve stock for order ${event.orderId}: insufficient inventory")
        }
    }
}
