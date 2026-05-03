package com.example.notificationservice.service

import com.example.notificationservice.model.Notification
import com.example.notificationservice.model.SendNotificationRequest
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

@Service
class NotificationService {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val notifications = ConcurrentHashMap<String, Notification>()

    fun sendNotification(request: SendNotificationRequest): Notification {
        val notification = Notification(
            customerId = request.customerId,
            subject = request.subject,
            message = request.message,
        )

        notifications[notification.id] = notification
        logger.info("Sending notification to customer ${notification.customerId}: ${notification.subject}")
        logger.info("Message: ${notification.message}")

        return notification
    }

    fun getNotificationsForCustomer(customerId: String): List<Notification> =
        notifications.values.filter { it.customerId == customerId }
}
