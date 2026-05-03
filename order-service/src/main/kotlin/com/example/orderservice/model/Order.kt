package com.example.orderservice.model

import java.time.Instant
import java.util.UUID

data class Order(
    val id: String = UUID.randomUUID().toString(),
    val customerId: String,
    val productId: String,
    val quantity: Int,
    val status: OrderStatus = OrderStatus.PENDING,
    val createdAt: Instant = Instant.now(),
)

enum class OrderStatus {
    PENDING,
    CONFIRMED,
    FAILED,
}

data class CreateOrderRequest(
    val customerId: String,
    val productId: String,
    val quantity: Int,
)
