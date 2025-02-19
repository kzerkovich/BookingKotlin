package domain.entities

import domain.Roles

data class User(
    var id: Int = UNDEFINED_ID,
    var login: String,
    var password: String,
    var email: String,
    var role: Roles = Roles.USER
) {
    companion object {
        const val UNDEFINED_ID = -1
    }
}
