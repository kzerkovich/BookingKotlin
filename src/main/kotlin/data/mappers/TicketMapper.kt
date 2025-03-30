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
        status = TicketStatusMapper.fromDbModelToEnum(row[TicketDbModel.status])
    )

    fun fromEntityToDbModel(ticket: Ticket): TicketDbModel.(InsertStatement<Number>) -> Unit = {
        TODO("The mapper from the application to the database is not working, most likely, the function signature" +
                " is incorrect")
//        it[eventId] = ticket.eventId
//        it[userId] = ticket.userId
//        it[status] = TicketStatusMapper.fromEnumToDbModel(ticket.status)
    }
}