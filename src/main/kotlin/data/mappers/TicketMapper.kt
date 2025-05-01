package data.mappers

import data.db.entities.TicketDbModel
import data.mappers.enums.TicketStatusMapper
import domain.entities.Ticket
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.InsertStatement

object TicketMapper {
    fun fromDbModelToEntity(row: ResultRow): Ticket = Ticket(
        id = row[TicketDbModel.id].value,
        eventId = row[TicketDbModel.eventId],
        userId = row[TicketDbModel.userId],
        bookingId = row[TicketDbModel.bookingId],
        status = TicketStatusMapper.fromDbModelToEnum(row[TicketDbModel.status]),
        purchaseDate = row[TicketDbModel.purchaseDate]?.let { Converter.convertTimestampToDate(it) }
    )

    fun fromEntityToDbModel(ticket: Ticket): TicketDbModel.(InsertStatement<Number>) -> Unit = {
        /*it[eventId] = ticket.eventId
        it[userId] = ticket.userId
        it[bookingId] = ticket.bookingId
        it[status] = TicketStatusMapper.fromEnumToDbModel(ticket.status)
        it[purchaseDate] = ticket.purchaseDate?.let { Converter.convertDateToTimestamp(it) }
         */
    }
}