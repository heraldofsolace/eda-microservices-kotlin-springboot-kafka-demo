package com.example.orderservice.service

import com.example.orderservice.event.OrderCreatedEvent
import com.example.orderservice.model.CreateOrderRequest
import com.example.orderservice.model.Order
import com.example.orderservice.model.OrderStatus
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

const val TOPIC_ORDERS_CREATED = "orders.created"

@Service
class OrderService(
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val objectMapper: ObjectMapper,
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

        logger.info("Order ${order.id} created for customer ${order.customerId}")

        val event = OrderCreatedEvent(
            orderId = order.id,
            customerId = order.customerId,
            productId = order.productId,
            quantity = order.quantity,
        )

        val payload = objectMapper.writeValueAsString(event)
        kafkaTemplate.send(TOPIC_ORDERS_CREATED, order.id, payload)
        logger.info("Published OrderCreatedEvent for order ${order.id} to topic $TOPIC_ORDERS_CREATED")

        orders[order.id] = order
        return order
    }

    fun getOrder(orderId: String): Order? = orders[orderId]

    fun getAllOrders(): List<Order> = orders.values.toList()
}
