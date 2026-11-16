package com.example.orderservice.service

import com.example.orderservice.event.OrderCreatedEvent
import com.example.orderservice.model.CreateOrderRequest
import com.example.orderservice.model.Order
import com.example.orderservice.model.OrderStatus
import kotlinx.coroutines.future.await
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.kafka.KafkaException
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.ResponseStatus
import java.util.concurrent.ConcurrentHashMap

const val TOPIC_ORDERS_CREATED = "orders.created"

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
class OrderPublishException(orderId: String, cause: Throwable) :
    RuntimeException("Could not publish OrderCreatedEvent for order $orderId", cause)

@Service
class OrderService(
    private val kafkaTemplate: KafkaTemplate<String, OrderCreatedEvent>,
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val orders = ConcurrentHashMap<String, Order>()

    suspend fun createOrder(request: CreateOrderRequest): Order {
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
        // await() suspends until the broker acknowledges the record, without blocking a thread.
        val result = try {
            kafkaTemplate.send(TOPIC_ORDERS_CREATED, order.id, event).await()
        } catch (exception: KafkaException) {
            logger.error("Failed to publish OrderCreatedEvent for order ${order.id}", exception)
            throw OrderPublishException(order.id, exception)
        }

        logger.info(
            "Published OrderCreatedEvent for order ${order.id} " +
                "to partition ${result.recordMetadata.partition()} " +
                "at offset ${result.recordMetadata.offset()}"
        )

        orders[order.id] = order
        return order
    }

    fun getOrder(orderId: String): Order? = orders[orderId]

    fun getAllOrders(): List<Order> = orders.values.toList()
}
