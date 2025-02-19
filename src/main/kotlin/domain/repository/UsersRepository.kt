package domain.repository

import domain.entities.User

interface UsersRepository {
    fun addUser(user: User)

    fun deleteUser(user: User)

    fun editUser(user: User)

    fun getUser(userId: Int): User

    fun getAllUsers(): List<User>
}