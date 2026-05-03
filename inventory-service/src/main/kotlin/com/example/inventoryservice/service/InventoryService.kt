package com.example.inventoryservice.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

@Service
class InventoryService {
    private val logger = LoggerFactory.getLogger(javaClass)

    private val stock = ConcurrentHashMap<String, AtomicInteger>().apply {
        put("PRODUCT-001", AtomicInteger(100))
        put("PRODUCT-002", AtomicInteger(50))
        put("PRODUCT-003", AtomicInteger(200))
    }

    fun getStock(productId: String): Int =
        stock[productId]?.get() ?: 0

    fun reserveStock(productId: String, quantity: Int): Pair<Boolean, Int> {
        val stockLevel = stock.getOrPut(productId) { AtomicInteger(0) }

        return synchronized(stockLevel) {
            val current = stockLevel.get()
            if (current >= quantity) {
                val remaining = stockLevel.addAndGet(-quantity)
                logger.info("Reserved $quantity units of $productId. Remaining: $remaining")
                true to remaining
            } else {
                logger.warn("Insufficient stock for $productId. Requested: $quantity, Available: $current")
                false to current
            }
        }
    }
}
