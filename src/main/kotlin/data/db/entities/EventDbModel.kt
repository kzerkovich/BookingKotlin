package data.db.entities

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object EventDbModel : IntIdTable("events") {
    val name = varchar("name", 100)
    val date = timestamp("date")
    val location = varchar("location", 200)
    val category = varchar("category", 50)
    val availableTickets = integer("available_tickets")
    val price = decimal("price", 19, 2)
}