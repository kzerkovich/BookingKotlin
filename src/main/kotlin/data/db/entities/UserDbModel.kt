package data.db.entities

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object UserDbModel : IntIdTable("users") {
    val username = varchar("username", 50).uniqueIndex()
    val password = varchar("password", 100)
    val email = varchar("email", 100)
    val role = varchar("role", 10)
    val bannedUntil = timestamp("banned_until").nullable()
}