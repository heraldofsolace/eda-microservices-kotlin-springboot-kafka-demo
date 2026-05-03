package com.example.inventoryservice.event

import java.time.Instant

data class OrderCreatedEvent(
    val orderId: String,
    val customerId: String,
    val productId: String,
    val quantity: Int,
    val timestamp: Instant,
)
