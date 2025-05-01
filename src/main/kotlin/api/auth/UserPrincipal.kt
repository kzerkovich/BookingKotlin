package api.auth

import domain.Roles
import io.ktor.server.auth.*

data class UserPrincipal(
    val role: Roles
) : Principal
