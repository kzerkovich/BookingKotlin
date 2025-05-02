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

            post {
                val request = call.receive<CreateUserRequest>()
                request.validate()

                val user = request.toEntity()
                val createdUser = userService.registerUser(user)
                call.respond(HttpStatusCode.Created, UserResponse(createdUser))
            }

            get("/{id}") {
                val userId = call.parameters["id"]?.toIntOrNull()
                    ?: throw IllegalArgumentException("Invalid user ID")

                val user = userService.getUser(userId)
                call.respond(UserResponse(user))
            }

            put("/{id}") {
                val userId = call.parameters["id"]?.toIntOrNull()
                    ?: throw IllegalArgumentException("Invalid user ID")

                val request = call.receive<UpdateUserRequest>()
                request.validate()

                val existingUser = userService.getUser(userId)
                val updatedUser = userService.updateUser(request.applyTo(existingUser))
                call.respond(UserResponse(updatedUser))
            }

            authenticate("auth-jwt") {
                delete("/{id}") {
                    val principal = call.principal<JWTPrincipal>()
                        ?: throw CustomExceptions.AuthenticationException("Not authenticated")

                    if (principal.payload.getClaim("role").asString() != Roles.ADMIN.name) {
                        throw CustomExceptions.ForbiddenException("Insufficient permissions")
                    }

                    val userId = call.parameters["id"]?.toInt()
                        ?: throw BadRequestException("Invalid ID")

                    userService.deleteUser(userId)
                    call.respond(HttpStatusCode.NoContent)
                }
            }

            authenticate("auth-jwt") {
                get("/role/{role}") {
                    val role = call.parameters["role"]?.let { Roles.valueOf(it.uppercase()) }
                        ?: throw IllegalArgumentException("Invalid role")

                    val users = userService.getUsersByRole(role)
                        .map { UserResponse(it) }
                    call.respond(users)
                }
            }

            post("/{id}/notifications") {
                val userId = call.parameters["id"]?.toIntOrNull()
                    ?: throw IllegalArgumentException("Invalid user ID")

                val enabled = call.request.queryParameters["enabled"]?.toBoolean()
                    ?: throw IllegalArgumentException("Enabled flag is required")

                val user = userService.toggleNotifications(userId, enabled)
                call.respond(UserResponse(user))
            }

            get("/{id}/ban-status") {
                val userId = call.parameters["id"]?.toIntOrNull()
                    ?: throw IllegalArgumentException("Некорректный ID пользователя")

                val isBanned = userService.checkUserBan(userId)
                val bannedUntil = userService.getUser(userId).bannedUntil?.time

                call.respond(BanStatusResponse(isBanned, bannedUntil))
            }
        }
    }
}