package domain.repository

import domain.entities.User
import java.util.*

interface UsersRepository {
    fun addUser(user: User) : User

    fun deleteUser(userId: Int) : Int

    fun editUser(user: User) : Int

    fun getUser(userId: Int): User

    fun getAllUsers(): List<User>

    fun updateUserBanStatus(userId: Int, bannedUntil: Date?): Int
    fun isUserBanned(userId: Int): Boolean
    fun existsByEmailOrPhone(email: String, phone: String): Boolean
}