package com.example.inventoryservice.consumer

import com.example.inventoryservice.event.OrderCreatedEvent
import com.example.inventoryservice.service.InventoryService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.stereotype.Component

@Component
class OrderEventConsumer(private val inventoryService: InventoryService) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @KafkaListener(
        topics = ["orders.created"],
        groupId = "\${spring.kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory",
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

        runBlocking {
            processOrder(event)
        }
    }

    private suspend fun processOrder(event: OrderCreatedEvent) {
        withContext(Dispatchers.IO) {
            val (success, remaining) = inventoryService.reserveStock(event.productId, event.quantity)

            if (success) {
                logger.info(
                    "Reserved ${event.quantity} units of ${event.productId} " +
                        "for order ${event.orderId}. Remaining: $remaining"
                )
            } else {
                logger.warn("Insufficient stock for order ${event.orderId} — throwing to trigger retry")
                throw IllegalStateException("Insufficient stock for product ${event.productId}")
            }
        }
    }
}
