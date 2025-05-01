package data.db.entities

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object TicketDbModel : IntIdTable("tickets") {
    val eventId = integer("event_id").references(EventDbModel.id)
    val userId = integer("user_id").references(UserDbModel.id)
    val bookingId = integer("booking_id").nullable()
    val status = varchar("status", 20)
    val purchaseDate = timestamp("purchase_date").nullable()
}