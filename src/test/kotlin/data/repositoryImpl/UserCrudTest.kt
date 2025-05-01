package data.repositoryImpl

import domain.Roles
import domain.entities.User
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*

class UserCrudTest : BaseCrudTest() {
    private val userRepo = UsersRepositoryImpl()

    private fun createTestUser(): User {
        return User(
            id = 0,
            login = "test_user_${UUID.randomUUID()}",
            password = "SecurePass123!",
            email = "test@example.com",
            role = Roles.USER
        )
    }

    @Test
    fun `create and read user`() {
        val user = createTestUser()

        val savedUser = userRepo.addUser(user)
        val foundUser = userRepo.getUser(savedUser.id)

        assertAll(
            { assertEquals(savedUser.id, foundUser.id) },
            { assertEquals(user.login, foundUser.login) },
            { assertEquals(user.email, foundUser.email) },
            { assertEquals(Roles.USER, foundUser.role) }
        )
    }

    @Test
    fun `update user email and role`() {
        val user = userRepo.addUser(createTestUser())
        val updatedUser = user.copy(
            email = "new_email@example.com",
            role = Roles.ADMIN
        )

        userRepo.editUser(updatedUser)
        val result = userRepo.getUser(user.id)

        assertAll(
            { assertEquals("new_email@example.com", result.email) },
            { assertEquals(Roles.ADMIN, result.role) }
        )
    }

    @Test
    fun `delete user`() {
        val user = userRepo.addUser(createTestUser())

        userRepo.deleteUser(user.id)

        assertThrows<NoSuchElementException> {
            userRepo.getUser(user.id)
        }
    }

    @Test
    fun `fail to create user with duplicate username`() {
        val username = "unique_user_${UUID.randomUUID()}"
        userRepo.addUser(createTestUser().copy(login = username))

        assertThrows<Exception> {
            userRepo.addUser(createTestUser().copy(login = username))
        }
    }
}