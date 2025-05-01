package api.dto

import domain.Roles
import domain.entities.User
import kotlinx.serialization.Serializable
import org.valiktor.functions.hasSize
import org.valiktor.functions.isEmail
import org.valiktor.functions.isNotBlank
import org.valiktor.validate

@Serializable
data class CreateUserRequest(
    val login: String,
    val password: String,
    val email: String,
    val notificationEnabled: Boolean = true
) {
    fun validate() = validate(this) {
        validate(CreateUserRequest::login).isNotBlank().hasSize(min = 3, max = 20)
        validate(CreateUserRequest::password).isNotBlank().hasSize(min = 6)
        validate(CreateUserRequest::email).isNotBlank().isEmail()
    }

    fun toEntity() = User(
        id = 0,
        login = login,
        password = password,
        email = email,
        notificationEnabled = notificationEnabled
    )
}

@Serializable
data class UpdateUserRequest(
    val login: String? = null,
    val password: String? = null,
    val email: String? = null,
    val notificationEnabled: Boolean? = null
) {
    fun validate() = validate(this) {
        login?.let { validate(UpdateUserRequest::login).isNotBlank().hasSize(min = 3, max = 20) }
        password?.let { validate(UpdateUserRequest::password).isNotBlank().hasSize(min = 6) }
        email?.let { validate(UpdateUserRequest::email).isNotBlank().isEmail() }
    }

    fun applyTo(user: User): User {
        login?.let { user.login = it }
        password?.let { user.password = it }
        email?.let { user.email = it }
        notificationEnabled?.let { user.notificationEnabled = it }
        return user
    }
}

@Serializable
data class UserResponse(
    val id: Int,
    val login: String,
    val email: String,
    val role: Roles,
    val bannedUntil: Long?,
    val notificationEnabled: Boolean
) {
    constructor(user: User) : this(
        id = user.id,
        login = user.login,
        email = user.email,
        role = user.role,
        bannedUntil = user.bannedUntil?.time,
        notificationEnabled = user.notificationEnabled
    )
}

@Serializable
data class BanStatusResponse(
    val isBanned: Boolean,
    val bannedUntil: Long?
)