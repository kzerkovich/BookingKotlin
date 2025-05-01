package domain

import kotlinx.serialization.Serializable

@Serializable
enum class Roles {
    USER, ADMIN
}