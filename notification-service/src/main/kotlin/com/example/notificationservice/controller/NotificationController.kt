package com.example.notificationservice.controller

import com.example.notificationservice.model.Notification
import com.example.notificationservice.model.SendNotificationRequest
import com.example.notificationservice.service.NotificationService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/notifications")
class NotificationController(private val notificationService: NotificationService) {

    @PostMapping
    fun sendNotification(@RequestBody request: SendNotificationRequest): ResponseEntity<Notification> {
        val notification = notificationService.sendNotification(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(notification)
    }

    @GetMapping("/customer/{customerId}")
    fun getNotificationsForCustomer(@PathVariable customerId: String): ResponseEntity<List<Notification>> =
        ResponseEntity.ok(notificationService.getNotificationsForCustomer(customerId))
}
