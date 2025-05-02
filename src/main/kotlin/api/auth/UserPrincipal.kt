package api.auth

import domain.Roles
import io.ktor.server.auth.*

data class UserPrincipal(
    val userId: Int,
    val role: Roles
) : Principal