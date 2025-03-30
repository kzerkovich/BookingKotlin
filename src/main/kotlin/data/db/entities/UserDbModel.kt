package data.db.entities

import org.jetbrains.exposed.dao.id.IntIdTable

object UserDbModel : IntIdTable("users") {
    val username = varchar("username", 50).uniqueIndex()
    val password = varchar("password", 100)
    val email = varchar("email", 100)
    val role = varchar("role", 10)
}