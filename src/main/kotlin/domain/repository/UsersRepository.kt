package domain.repository

import domain.entities.User

interface UsersRepository {
    fun addUser(user: User) : User

    fun deleteUser(userId: Int) : Int

    fun editUser(user: User) : Int

    fun getUser(userId: Int): User

    fun getAllUsers(): List<User>
}