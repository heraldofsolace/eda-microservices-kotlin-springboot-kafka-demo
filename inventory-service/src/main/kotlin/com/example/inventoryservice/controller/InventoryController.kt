package com.example.inventoryservice.controller

import com.example.inventoryservice.model.ReserveStockRequest
import com.example.inventoryservice.model.ReserveStockResponse
import com.example.inventoryservice.service.InventoryService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/inventory")
class InventoryController(private val inventoryService: InventoryService) {

    @GetMapping("/{productId}/stock")
    fun getStock(@PathVariable productId: String): ResponseEntity<Map<String, Any>> {
        val stock = inventoryService.getStock(productId)
        return ResponseEntity.ok(mapOf("productId" to productId, "availableStock" to stock))
    }

    @PostMapping("/reserve")
    fun reserveStock(@RequestBody request: ReserveStockRequest): ResponseEntity<ReserveStockResponse> {
        val (success, remaining) = inventoryService.reserveStock(request.productId, request.quantity)
        return ResponseEntity.ok(ReserveStockResponse(success = success, remainingStock = remaining))
    }
}
