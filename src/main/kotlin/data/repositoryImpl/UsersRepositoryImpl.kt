package data.repositoryImpl

import domain.entities.User
import domain.repository.UsersRepository

class UsersRepositoryImpl(): UsersRepository {
    var usersList = mutableListOf<User>()
    private var autoIncrementId = 0

    override fun addUser(user: User) {
        if (user.id == User.UNDEFINED_ID)
            user.id = autoIncrementId++
        usersList.add(user)
    }

    override fun deleteUser(user: User) {
        val id = usersList.indexOf(user)
        usersList.removeAt(id)
    }

    override fun editUser(user: User) {
        val oldUser = getUser(user.id)
        usersList.remove(oldUser)
        addUser(user)
    }

    override fun getUser(userId: Int): User {
        return usersList.find {
            it.id == userId
        } ?: throw RuntimeException("Element with id = $userId not found")
    }

    override fun getAllUsers(): List<User> {
        return usersList
    }
}