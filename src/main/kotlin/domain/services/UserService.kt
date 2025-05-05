package domain.services

import domain.Roles
import domain.entities.User
import domain.repository.UsersRepository
import java.nio.file.AccessDeniedException

class UserService(
    private val userRepository: UsersRepository
) {
    fun registerUser(user: User): User {
        println("Registering new user: ${user.login}")
        return try {
            if (userRepository.getAllUsers().any { it.login == user.login }) {
                println("Registration failed: Login ${user.login} already exists")
                throw IllegalArgumentException("Login already exists")
            }
            val createdUser = userRepository.addUser(user)
            println("User ${createdUser.id} registered successfully")
            createdUser
        } catch (e: Exception) {
            println("User registration error")
            throw e
        }
    }

    fun updateUser(user: User): User {
        println("Updating user ${user.id}")
        val existing = userRepository.getUser(user.id)

        if (existing.role != user.role) {
            println("Role change attempt detected for user ${user.id}")
            throw AccessDeniedException("Role cannot be changed manually")
        }
        userRepository.editUser(user)
        println("User ${user.id} data updated")
        return user
    }

    fun deleteUser(userId: Int): Int {
        println("Deleting user $userId")
        val result = userRepository.deleteUser(userId)
        println("User $userId deleted (result code: $result)")
        return result
    }

    fun getUser(userId: Int): User {
        println("Fetching user $userId")
        return userRepository.getUser(userId).also {
            println("Retrieved user: ${it.login} (ID: $userId)")
        }
    }

    fun getUsersByRole(role: Roles): List<User> {
        println("Searching users with role $role")
        val users = userRepository.getAllUsers().filter { it.role == role }
        println("Found ${users.size} users with role $role")
        return users
    }

    fun checkUserExists(email: String, phone: String): Boolean {
        return userRepository.existsByEmailOrPhone(email, phone)
    }

    fun toggleNotifications(userId: Int, enabled: Boolean): User {
        println("Toggling notifications for user $userId to $enabled")
        val user = userRepository.getUser(userId)
        user.notificationEnabled = enabled
        userRepository.editUser(user)
        println("Notifications for user $userId set to $enabled")
        return user
    }

    fun checkUserBan(userId: Int): Boolean {
        println("Checking ban status for user $userId")
        return userRepository.isUserBanned(userId).also { banned ->
            println("User $userId banned status: $banned")
        }
    }
}