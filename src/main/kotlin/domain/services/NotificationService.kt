package domain.services

import domain.entities.User

class NotificationService {
    fun notifyUser(user: User, message: String) {
        println("Notification to ${user.email}: $message")
    }
}