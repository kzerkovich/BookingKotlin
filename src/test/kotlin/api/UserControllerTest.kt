package api

import api.auth.SecurityConfiguration
import api.controllers.UserController
import api.dto.*
import domain.Roles
import domain.Serializer
import domain.entities.User
import domain.services.UserService
import exception.CustomExceptions
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import io.mockk.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import org.junit.jupiter.api.Test
import org.valiktor.ConstraintViolationException
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class UserControllerTest {
    private val mockUserService = mockk<UserService>()
    private val userController = UserController(mockUserService)

    @Test
    fun `POST users should create user`() = testApplication {
        val testUser = User(
            id = 1,
            login = "testUser",
            password = "password123",
            email = "test@example.com"
        )

        every { mockUserService.registerUser(any()) } returns testUser

        application {
            install(io.ktor.server.plugins.contentnegotiation.ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
            install(Authentication) {
                SecurityConfiguration.apply { configureAuth() }
            }
            routing {
                userController.apply { registerRoutes() }
            }
        }

        val client = createClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
        }

        val request = CreateUserRequest(
            login = "testUser",
            password = "password123",
            email = "test@example.com"
        )

        val response = client.post("/users") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }

        assertEquals(HttpStatusCode.Created, response.status)
        val responseBody = response.body<UserResponse>()
        assertEquals(testUser.id, responseBody.id)
        verify(exactly = 1) { mockUserService.registerUser(any()) }
    }

    @Test
    fun `POST users rejects invalid data`() = testApplication {
        application {
            install(Authentication) {
                SecurityConfiguration.apply { configureAuth() }
            }
            install(io.ktor.server.plugins.contentnegotiation.ContentNegotiation) {
                jackson()
            }
            routing { userController.apply { registerRoutes() } }
        }

        val client = createClient {
            install(ContentNegotiation) {
                jackson()
            }
        }

        listOf(
            CreateUserRequest("te", "short", "invalid-email"),
            CreateUserRequest("validLogin", "short", "test@example.com"),
            CreateUserRequest("validLogin", "validPassword", "invalid")
        ).forEach { badRequest ->
            assertFailsWith<ConstraintViolationException> {
                client.post("/users") {
                    contentType(ContentType.Application.Json)
                    setBody(badRequest)
                }
            }
        }
    }

    @Test
    fun `GET user by id returns correct user`() = testApplication {
        val testUser = createTestUser()
        every { mockUserService.getUser(1) } returns testUser

        application {
            install(Authentication) {
                SecurityConfiguration.apply { configureAuth() }
            }
            install(io.ktor.server.plugins.contentnegotiation.ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
            routing {
                userController.apply { registerRoutes() }
            }
        }

        val client = createClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
        }

        val response = client.get("/users/1") {
            header(HttpHeaders.Accept, ContentType.Application.Json)
        }

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(testUser.id, response.body<UserResponse>().id)

        assertFailsWith<IllegalArgumentException> {
            client.get("/users/invalid")
        }
    }

    @Test
    fun `PUT user updates existing user`() = testApplication {
        val testUser = createTestUser()
        every { mockUserService.getUser(1) } returns testUser
        every { mockUserService.updateUser(any()) } returnsArgument 0

        application {
            install(Authentication) {
                SecurityConfiguration.apply { configureAuth() }
            }
            install(io.ktor.server.plugins.contentnegotiation.ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    serializersModule = SerializersModule {
                        contextual(Date::class, Serializer)
                    }
                })
            }
            routing {
                userController.apply { registerRoutes() }
            }
        }

        val client = createClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    serializersModule = SerializersModule {
                        contextual(Date::class, Serializer)
                    }
                })
            }
        }

        val updateRequest = UpdateUserRequest(
            login = "newLogin",
            email = "new@example.com"
        )

        val response = client.put("/users/1") {
            contentType(ContentType.Application.Json)
            setBody(updateRequest)
        }

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("newLogin", response.body<UserResponse>().login)
        verify(exactly = 1) { mockUserService.updateUser(any()) }
    }

    @Test
    fun `DELETE user requires admin auth`() = testApplication {
        install(io.ktor.server.plugins.contentnegotiation.ContentNegotiation) {
            json(Json)
        }
        install(Authentication) {
            SecurityConfiguration.apply { configureAuth() }
        }

        coEvery { mockUserService.getUser(1) } returns User(
            id = 1,
            login = "admin",
            password = "hash",
            email = "admin@test.com",
            role = Roles.ADMIN
        )
        coEvery { mockUserService.deleteUser(1) } returns 1

        val adminToken = SecurityConfiguration.generateToken(1, Roles.ADMIN.name)
        val successResponse = client.delete("/users/1") {
            header(HttpHeaders.Authorization, "Bearer $adminToken")
        }
        assertEquals(HttpStatusCode.NotFound, successResponse.status)
    }

    @Test
    fun `GET users by role returns filtered list`() = testApplication {
        val testUsers = listOf(createTestUser().copy(role = Roles.ADMIN))
        every { mockUserService.getUsersByRole(Roles.ADMIN) } returns testUsers

        setupApplication {
            install(Authentication) {
                SecurityConfiguration.apply { configureAuth() }
            }
        }

        val client = createClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
        }

        val adminToken = SecurityConfiguration.generateToken(
            userId = 1,
            role = Roles.ADMIN.name
        )

        val response = client.get("/users/role/ADMIN") {
            header(HttpHeaders.Authorization, "Bearer $adminToken")
            header(HttpHeaders.Accept, ContentType.Application.Json)
        }

        val users = response.body<List<UserResponse>>()
        assertEquals(1, users.size)
        assertEquals(Roles.ADMIN, users[0].role)
    }

    @Test
    fun `POST toggle notifications updates setting`() = testApplication {
        val testUser = createTestUser()
        every { mockUserService.toggleNotifications(1, false) } returns testUser.copy(notificationEnabled = false)

        application {
            install(Authentication) {
                SecurityConfiguration.apply { configureAuth() }
            }
            install(io.ktor.server.plugins.contentnegotiation.ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
            routing {
                userController.apply { registerRoutes() }
            }
        }

        val client = createClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
        }

        val response = client.post("/users/1/notifications?enabled=false")
        assertEquals(false, response.body<UserResponse>().notificationEnabled)
    }

    @Test
    fun `GET ban status returns correct info`() = testApplication {
        val bannedUser = createTestUser().apply {
            bannedUntil = Date(System.currentTimeMillis() + 100000)
        }

        every { mockUserService.checkUserBan(1) } returns true
        every { mockUserService.getUser(1) } returns bannedUser

        application {
            install(Authentication) {
                SecurityConfiguration.apply { configureAuth() }
            }
            install(io.ktor.server.plugins.contentnegotiation.ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    serializersModule = SerializersModule {
                        contextual(Date::class, Serializer)
                    }
                })
            }
            routing {
                userController.apply { registerRoutes() }
            }
        }

        val client = createClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    serializersModule = SerializersModule {
                        contextual(Date::class, Serializer)
                    }
                })
            }
        }

        val response = client.get("/users/1/ban-status") {
            header(HttpHeaders.Accept, ContentType.Application.Json)
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val banStatus = response.body<BanStatusResponse>()
        assertEquals(true, banStatus.isBanned)
    }

    private fun TestApplicationBuilder.setupApplication(
        authConfig: Application.() -> Unit = {}
    ) {
        application {
            install(io.ktor.server.plugins.contentnegotiation.ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
            install(StatusPages)
            authConfig()
            routing {
                userController.apply { registerRoutes() }
            }
        }
    }

    private fun createTestUser() = User(
        id = 1,
        login = "testUser",
        password = "password123",
        email = "test@example.com",
        role = Roles.USER,
        notificationEnabled = true
    )
}