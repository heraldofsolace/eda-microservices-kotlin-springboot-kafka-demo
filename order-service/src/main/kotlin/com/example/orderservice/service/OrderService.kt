package com.example.orderservice.service

import com.example.orderservice.event.OrderCreatedEvent
import com.example.orderservice.model.CreateOrderRequest
import com.example.orderservice.model.Order
import com.example.orderservice.model.OrderStatus
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

const val TOPIC_ORDERS_CREATED = "orders.created"

@Service
class OrderService(
    private val kafkaTemplate: KafkaTemplate<String, OrderCreatedEvent>,
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val orders = ConcurrentHashMap<String, Order>()

    fun createOrder(request: CreateOrderRequest): Order {
        val order = Order(
            customerId = request.customerId,
            productId = request.productId,
            quantity = request.quantity,
            status = OrderStatus.PENDING,
        )

        val event = OrderCreatedEvent(
            orderId = order.id,
            customerId = order.customerId,
            productId = order.productId,
            quantity = order.quantity,
        )

        // Use orderId as the message key so all events for one order land on the same partition,
        // preserving ordering guarantees per order.
        kafkaTemplate.send(TOPIC_ORDERS_CREATED, order.id, event)
            .whenComplete { result, exception ->
                if (exception != null) {
                    logger.error("Failed to publish OrderCreatedEvent for order ${order.id}", exception)
                } else {
                    logger.info(
                        "Published OrderCreatedEvent for order ${order.id} " +
                            "to partition ${result.recordMetadata.partition()} " +
                            "at offset ${result.recordMetadata.offset()}"
                    )
                }
            }

        orders[order.id] = order
        return order
    }

    fun getOrder(orderId: String): Order? = orders[orderId]

    fun getAllOrders(): List<Order> = orders.values.toList()
}
