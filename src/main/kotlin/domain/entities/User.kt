package domain.entities

import domain.Roles

data class User(
    var id: Int,
    var login: String,
    var password: String,
    var email: String,
    var role: Roles = Roles.USER
)
