package domain.services

import domain.entities.User

class NotificationService {
    fun notifyUser(user: User, message: String) {
        println("Sending notification to ${user.email} [UserID: ${user.id}]")
        println("Notification content: $message")
        println("Notification to ${user.email}: $message")
    }
}