package data.mappers.enums

import domain.Roles

object RolesMapper {
    fun fromEnumToDbModel(role: Roles): String {
        return when (role) {
            Roles.USER -> "USER"
            Roles.ADMIN -> "ADMIN"
            else -> throw Exception("Unresolved role: $role")
        }
    }

    fun fromDbModelToEnum(role: String): Roles {
        return when (role) {
            "USER" -> Roles.USER
            "ADMIN" -> Roles.ADMIN
            else -> throw Exception("Unresolved role: $role")
        }
    }
}