package com.example.orderservice.service

import com.example.orderservice.client.InventoryClient
import com.example.orderservice.client.NotificationClient
import com.example.orderservice.model.CreateOrderRequest
import com.example.orderservice.model.Order
import com.example.orderservice.model.OrderStatus
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

@Service
class OrderService(
    private val inventoryClient: InventoryClient,
    private val notificationClient: NotificationClient,
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val orders = ConcurrentHashMap<String, Order>()

    fun createOrder(request: CreateOrderRequest): Order {
        val order = Order(
            customerId = request.customerId,
            productId = request.productId,
            quantity = request.quantity,
        )

        logger.info("Creating order ${order.id} for customer ${order.customerId}")

        val reservationResult = inventoryClient.reserveStock(order.productId, order.quantity)

        val finalOrder = if (reservationResult.success) {
            logger.info(
                "Stock reserved for order ${order.id}. " +
                    "Remaining stock: ${reservationResult.remainingStock}"
            )

            notificationClient.sendNotification(
                customerId = order.customerId,
                subject = "Order Confirmed",
                message = "Your order ${order.id} for ${order.quantity}x ${order.productId} has been confirmed.",
            )

            order.copy(status = OrderStatus.CONFIRMED)
        } else {
            logger.warn("Insufficient stock for order ${order.id}")
            order.copy(status = OrderStatus.FAILED)
        }

        orders[finalOrder.id] = finalOrder
        return finalOrder
    }

    fun getOrder(orderId: String): Order? = orders[orderId]

    fun getAllOrders(): List<Order> = orders.values.toList()
}
