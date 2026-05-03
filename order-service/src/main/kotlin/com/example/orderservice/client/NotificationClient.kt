package com.example.orderservice.client

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.body

data class SendNotificationRequest(
    val customerId: String,
    val subject: String,
    val message: String,
)

@Component
class NotificationClient(
    restClientBuilder: RestClient.Builder,
    @param:Value("\${services.notification.url}") private val notificationUrl: String,
) {
    private val restClient = restClientBuilder.baseUrl(notificationUrl).build()

    fun sendNotification(customerId: String, subject: String, message: String) {
        restClient
            .post()
            .uri("/notifications")
            .body(SendNotificationRequest(customerId, subject, message))
            .retrieve()
            .body<Unit>()
    }
}
