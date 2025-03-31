package api.controllers

import domain.entities.User
import domain.repository.UsersRepository
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.valiktor.validate
import org.valiktor.functions.*

fun Route.userController(repository: UsersRepository) {
    route("/users") {
        post {
            val user = call.receive<User>().apply {
                validate(this) {
                    validate(User::login).hasSize(min = 3, max = 20)
                    validate(User::password).hasSize(min = 8)
                    validate(User::email).isEmail()
                }
            }
            val createdUser = repository.addUser(user)
            call.respond(createdUser)
        }

        get {
            val role = call.request.queryParameters["role"]
            val users = repository.getAllUsers().filter { role == null || it.role.name == role }
            call.respond(users)
        }

        route("/{userId}") {
            get {
                val userId = call.parameters["userId"]?.toIntOrNull() ?: throw IllegalArgumentException("Invalid ID")
                val user = repository.getUser(userId)
                call.respond(user)
            }

            put {
                val userId = call.parameters["userId"]?.toIntOrNull() ?: throw IllegalArgumentException("Invalid ID")
                val user = call.receive<User>().copy(id = userId)
                repository.editUser(user)
                call.respond(mapOf("message" to "User updated"))
            }

            delete {
                val userId = call.parameters["userId"]?.toIntOrNull() ?: throw IllegalArgumentException("Invalid ID")
                repository.deleteUser(userId)
                call.respond(mapOf("message" to "User deleted"))
            }
        }
    }
}