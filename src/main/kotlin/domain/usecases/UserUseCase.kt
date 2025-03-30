package domain.usecases

import domain.Roles
import domain.entities.User
import domain.repository.UsersRepository
import java.nio.file.AccessDeniedException

class UserUseCase(
    private val userRepository: UsersRepository
) {
    fun registerUser(user: User): User {
        if (userRepository.getAllUsers().any { it.login == user.login }) {
            throw IllegalArgumentException("Login already exists")
        }
        return userRepository.addUser(user)
    }

    fun updateUser(user: User): User {
        val existing = userRepository.getUser(user.id)
        if (existing.role != user.role) {
            throw AccessDeniedException("Role cannot be changed manually")
        }
        userRepository.editUser(user)
        return user
    }

    fun deleteUser(userId: Int): Int {
        return userRepository.deleteUser(userId)
    }

    fun getUser(userId: Int): User = userRepository.getUser(userId)

    fun getUsersByRole(role: Roles): List<User> {
        return userRepository.getAllUsers().filter { it.role == role }
    }
}