package api.controllers

import api.dto.BanStatusResponse
import api.dto.CreateUserRequest
import api.dto.UpdateUserRequest
import api.dto.UserResponse
import domain.Roles
import domain.services.UserService
import exception.CustomExceptions
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

class UserController(private val userService: UserService) {
    fun Route.registerRoutes() {
        route("/users") {

            post("/check-auth") {
                try {
                    val params = call.receiveParameters()
                    val email = params["email"] ?: throw IllegalArgumentException("Email required")
                    val phone = params["phone"] ?: "" // Если нужно проверять телефон

                    val exists = userService.checkUserExists(email, phone)

                    if (exists) {
                        println("Успех: Пользователь с email $email найден")
                        call.respond(mapOf("status" to "Успех"))
                    } else {
                        println("Ошибка: Пользователь не найден")
                        call.respond(mapOf("status" to "Ошибка"))
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                }
            }

            post {
                try {
                    val request = call.receive<CreateUserRequest>()
                    println("POST /users | Login: ${request.login}")
                    request.validate()

                    val user = request.toEntity()
                    val createdUser = userService.registerUser(user)
                    println("User ${createdUser.id} registered")
                    call.respond(HttpStatusCode.Created, UserResponse(createdUser))
                } catch (e: Exception) {
                    println("User registration failed: ${e.message}")
                    throw e
                }
            }

            get("/{id}") {
                try {
                    val userId = call.parameters["id"]?.toIntOrNull()
                        ?: throw IllegalArgumentException("Invalid user ID").also {
                            println("Missing user ID parameter")
                        }

                    println("GET /users/$userId")
                    val user = userService.getUser(userId)
                    println("Retrieved user ${user.login} (ID: $userId)")
                    call.respond(UserResponse(user))
                } catch (e: Exception) {
                    println("Failed to get user ")
                    throw e
                }
            }

            put("/{id}") {
                try {
                    val userId = call.parameters["id"]?.toIntOrNull()
                        ?: throw IllegalArgumentException("Invalid user ID").also {
                            println("Missing user ID parameter")
                        }

                    val request = call.receive<UpdateUserRequest>()
                    println("PUT /users/$userId | Updates: ${request}")
                    request.validate()

                    val existingUser = userService.getUser(userId)
                    val updatedUser = userService.updateUser(request.applyTo(existingUser))
                    println("User $userId updated")
                    call.respond(UserResponse(updatedUser))
                } catch (e: Exception) {
                    println("Failed to update user")
                    throw e
                }
            }

            authenticate("auth-jwt") {
                delete("/{id}") {
                    try {
                        val principal = call.principal<JWTPrincipal>()
                            ?: throw CustomExceptions.AuthenticationException("Not authenticated").also {
                                println("Unauthorized delete attempt")
                            }

                        if (principal.payload.getClaim("role").asString() != Roles.ADMIN.name) {
                            println(
                                "Forbidden delete attempt by ${
                                    principal.payload.getClaim("role").asString()
                                }"
                            )
                            throw CustomExceptions.ForbiddenException("Insufficient permissions")
                        }

                        val userId = call.parameters["id"]?.toInt()
                            ?: throw BadRequestException("Invalid ID").also {
                                println("Invalid user ID format")
                            }

                        println("DELETE /users/$userId by ${principal.payload.getClaim("role").asString()}")
                        userService.deleteUser(userId)
                        println("User $userId deleted by admin")
                        call.respond(HttpStatusCode.NoContent)
                    } catch (e: Exception) {
                        println("User deletion failed")
                        throw e
                    }
                }
            }

            authenticate("auth-jwt") {
                get("/role/{role}") {
                    try {
                        val role = call.parameters["role"]?.let { Roles.valueOf(it.uppercase()) }
                            ?: throw IllegalArgumentException("Invalid role").also {
                                println("Missing role parameter")
                            }
                        println("GET /users/role/$role by ${call.principal<JWTPrincipal>()?.payload?.getClaim("role")}")

                        val users = userService.getUsersByRole(role)
                            .map { UserResponse(it) }
                        println("Found ${users.size} users with role $role")
                        call.respond(users)
                    } catch (e: Exception) {
                        println("Failed to get users by role")
                        throw e
                    }
                }
            }

            post("/{id}/notifications") {
                try {
                    val userId = call.parameters["id"]?.toIntOrNull()
                        ?: throw IllegalArgumentException("Invalid user ID").also {
                            println("Missing user ID parameter")
                        }

                    val enabled = call.request.queryParameters["enabled"]?.toBoolean()
                        ?: throw IllegalArgumentException("Enabled flag is required").also {
                            println("Missing enabled parameter")
                        }
                    println("POST /users/$userId/notifications | Enabled: $enabled")
                    val user = userService.toggleNotifications(userId, enabled)
                    println("Notifications for user $userId set to $enabled")
                    call.respond(UserResponse(user))
                } catch (e: Exception) {
                    println("Failed to toggle notifications")
                    throw e
                }
            }

            get("/{id}/ban-status") {
                try {
                    val userId = call.parameters["id"]?.toIntOrNull()
                        ?: throw IllegalArgumentException("Некорректный ID пользователя").also {
                            println("Missing user ID parameter")
                        }

                    println("GET /users/$userId/ban-status")
                    val isBanned = userService.checkUserBan(userId)
                    val bannedUntil = userService.getUser(userId).bannedUntil?.time

                    println("Ban status for user $userId: $isBanned")
                    call.respond(BanStatusResponse(isBanned, bannedUntil))
                } catch (e: Exception) {
                    println("Failed to get ban status")
                    throw e
                }
            }
        }
    }
}