package data.repositoryImpl

import domain.Roles
import domain.entities.User
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName

class UsersRepositoryImplTest {
    private val testUsersRepositoryImpl: UsersRepositoryImpl = UsersRepositoryImpl()

    private val testUser1 = User(
        id = -1,
        email = "testEmail",
        login = "testLogin",
        password = "testPassword"
    )
    private val testUser2 = User(
        id = -1,
        email = "testEmail",
        login = "testLogin",
        password = "testPassword"
    )
    private val testUser3 = User(
        id = 10,
        email = "testEmail",
        login = "testLogin",
        password = "testPassword"
    )
    private val testUserEdited = User(
        id = 10,
        email = "testEmail",
        login = "testLogin",
        password = "testPassword",
        role = Roles.ADMIN
    )

    @Test
    @DisplayName("addUser test")
    fun addUser() {
        testUsersRepositoryImpl.addUser(testUser1)
        testUsersRepositoryImpl.addUser(testUser2)
        testUsersRepositoryImpl.addUser(testUser3)

        assertEquals(3, testUsersRepositoryImpl.usersList.size)
    }

    @Test
    @DisplayName("deleteUser test")
    fun deleteUser() {
        testUsersRepositoryImpl.addUser(testUser1)
        testUsersRepositoryImpl.addUser(testUser2)
        testUsersRepositoryImpl.addUser(testUser3)

        testUsersRepositoryImpl.deleteUser(testUser1)
        assertEquals(2, testUsersRepositoryImpl.usersList.size)

        testUsersRepositoryImpl.deleteUser(testUser2)
        assertEquals(1, testUsersRepositoryImpl.usersList.size)

        testUsersRepositoryImpl.deleteUser(testUser3)
        assertEquals(0, testUsersRepositoryImpl.usersList.size)
    }

    @Test
    @DisplayName("editUser test")
    fun editUser() {
        testUsersRepositoryImpl.addUser(testUser1)
        testUsersRepositoryImpl.addUser(testUser2)
        testUsersRepositoryImpl.addUser(testUser3)

        testUsersRepositoryImpl.editUser(testUserEdited)
        assertEquals(testUsersRepositoryImpl.getUser(10).role, Roles.ADMIN)
    }

    @Test
    @DisplayName("getUser test")
    fun getUser() {
        testUsersRepositoryImpl.addUser(testUser1)
        testUsersRepositoryImpl.addUser(testUser2)
        testUsersRepositoryImpl.addUser(testUser3)

        assertEquals(testUser1, testUsersRepositoryImpl.getUser(0))
        assertEquals(testUser2, testUsersRepositoryImpl.getUser(1))
        assertEquals(testUser3, testUsersRepositoryImpl.getUser(10))
    }

    @Test
    @DisplayName("getAllUsers test")
    fun getAllUsers() {
        testUsersRepositoryImpl.addUser(testUser1)
        testUsersRepositoryImpl.addUser(testUser2)
        testUsersRepositoryImpl.addUser(testUser3)

        assertEquals(mutableListOf(testUser1, testUser2, testUser3), testUsersRepositoryImpl.getAllUsers())

    }
}