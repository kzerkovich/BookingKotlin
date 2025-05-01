package data.services

import domain.entities.User
import domain.Roles
import domain.repository.UsersRepository
import domain.services.UserService
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import java.nio.file.AccessDeniedException

class UserServiceTest {
    private val userRepository = mockk<UsersRepository>()
    private lateinit var userService: UserService

    @BeforeEach
    fun setup() {
        val testModule = module {
            single { userRepository }
            single { UserService(get()) }
        }
        startKoin { modules(testModule) }
        userService = UserService(userRepository)
    }

    @Test
    fun `registerUser should throw error if login exists`() {
        val existingUser = User(
            id = 1,
            login = "testUser",
            password = "pass",
            email = "test@test.com",
            role = Roles.USER
        )
        every { userRepository.getAllUsers() } returns listOf(existingUser)

        assertThrows<IllegalArgumentException> {
            userService.registerUser(existingUser.copy(id = 2))
        }
    }

    @Test
    fun `updateUser should forbid role change`() {
        val originalUser = User(
            id = 1,
            login = "testUser",
            password = "pass",
            email = "test@test.com",
            role = Roles.ADMIN
        )
        val updatedUser = originalUser.copy(role = Roles.USER)
        every { userRepository.getUser(1) } returns originalUser

        assertThrows<AccessDeniedException> {
            userService.updateUser(updatedUser)
        }
    }

    @AfterEach
    fun tearDown() {
        stopKoin()
    }
}