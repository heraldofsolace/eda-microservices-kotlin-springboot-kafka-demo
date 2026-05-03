package com.example.notificationservice.model

import java.time.Instant
import java.util.UUID

data class Notification(
    val id: String = UUID.randomUUID().toString(),
    val customerId: String,
    val subject: String,
    val message: String,
    val sentAt: Instant = Instant.now(),
)

data class SendNotificationRequest(
    val customerId: String,
    val subject: String,
    val message: String,
)
