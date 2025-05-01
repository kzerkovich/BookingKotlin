package api.controllers

import domain.Roles
import domain.entities.User
import domain.services.UserService
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.valiktor.validate
import org.valiktor.functions.*

class UserController(private val userService: UserService) {

}