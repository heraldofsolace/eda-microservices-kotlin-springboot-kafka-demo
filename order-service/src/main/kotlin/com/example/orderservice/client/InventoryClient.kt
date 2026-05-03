package com.example.orderservice.client

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.body

data class ReserveStockRequest(val productId: String, val quantity: Int)
data class ReserveStockResponse(val success: Boolean, val remainingStock: Int)

@Component
class InventoryClient(
    restClientBuilder: RestClient.Builder,
    @param:Value("\${services.inventory.url}") private val inventoryUrl: String,
) {
    private val restClient = restClientBuilder.baseUrl(inventoryUrl).build()

    fun reserveStock(productId: String, quantity: Int): ReserveStockResponse =
        restClient
            .post()
            .uri("/inventory/reserve")
            .body(ReserveStockRequest(productId, quantity))
            .retrieve()
            .body<ReserveStockResponse>()
            ?: throw IllegalStateException("No response from inventory service")
}
