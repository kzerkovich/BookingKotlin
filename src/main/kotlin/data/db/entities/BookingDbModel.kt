package data.db.entities

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object BookingDbModel : IntIdTable("bookings") {
    val eventId = integer("event_id").references(EventDbModel.id)
    val userId = integer("user_id").references(UserDbModel.id)
    val ticketsCount = integer("tickets_count")
    val bookingDate = timestamp("booking_date")
    val expirationDate = timestamp("expiration_date")
    val status = varchar("status", 20)
}