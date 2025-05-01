package domain.entities

import domain.Roles
import domain.Serializer
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class User(
    var id: Int,
    var login: String,
    var password: String,
    var email: String,
    @Serializable(with = Serializer::class)
    var bannedUntil: Date? = null,
    var role: Roles = Roles.USER,
    var notificationEnabled: Boolean = true
)
