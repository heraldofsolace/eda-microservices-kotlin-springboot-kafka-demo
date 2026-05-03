package com.example.inventoryservice.model

data class InventoryItem(
    val productId: String,
    val productName: String,
    val availableStock: Int,
)

data class ReserveStockRequest(val productId: String, val quantity: Int)

data class ReserveStockResponse(val success: Boolean, val remainingStock: Int)
