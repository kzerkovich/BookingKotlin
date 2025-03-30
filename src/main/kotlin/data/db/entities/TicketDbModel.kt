package data.db.entities

import org.jetbrains.exposed.dao.id.IntIdTable

object TicketDbModel : IntIdTable("tickets") {
    val eventId = integer("event_id").references(EventDbModel.id)
    val userId = integer("user_id").references(UserDbModel.id)
    val status = varchar("status", 20)
}