package data.repositoryImpl

import data.db.entities.TicketDbModel
import data.mappers.Converter
import data.mappers.TicketMapper
import data.mappers.enums.TicketStatusMapper
import domain.TicketStatus
import domain.entities.Ticket
import domain.repository.TicketRepository
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

class TicketRepositoryImpl : TicketRepository {
    override fun addTicket(ticket: Ticket): Ticket = transaction {
        val id = TicketDbModel.insertAndGetId {
            it[eventId] = ticket.eventId
            it[userId] = ticket.userId
            it[bookingId] = ticket.bookingId
            it[status] = TicketStatusMapper.fromEnumToDbModel(ticket.status)
            it[purchaseDate] = ticket.purchaseDate?.let { Converter.convertDateToTimestamp(it) }
        }.value
        ticket.copy(id = id)
    }

    override fun editTicket(ticket: Ticket) = transaction {
        TicketDbModel.update({ TicketDbModel.id eq ticket.id }) {
            it[eventId] = ticket.eventId
            it[userId] = ticket.userId
            it[bookingId] = ticket.bookingId
            it[status] = TicketStatusMapper.fromEnumToDbModel(ticket.status)
            it[purchaseDate] = ticket.purchaseDate?.let { Converter.convertDateToTimestamp(it) }
        }
    }

    override fun deleteTicket(ticketId: Int) = transaction {
        TicketDbModel.deleteWhere { TicketDbModel.id eq ticketId }
    }



    override fun getTicket(ticketId: Int): Ticket = transaction {
        TicketDbModel.selectAll().where { TicketDbModel.id eq ticketId }
            .map { row ->
                TicketMapper.fromDbModelToEntity(row)
            }.firstOrNull() ?: throw NoSuchElementException("Ticket $ticketId not found")
    }

    override fun getAllTickets(): List<Ticket> = transaction {
        TicketDbModel.selectAll().map { row ->
            TicketMapper.fromDbModelToEntity(row)
        }
    }

    override fun getTicketsByBooking(bookingId: Int): List<Ticket> = transaction {
        TicketDbModel.selectAll().where { TicketDbModel.bookingId eq bookingId }
            .map { row -> TicketMapper.fromDbModelToEntity(row) }
    }

    override fun updateTicketsStatus(bookingId: Int, status: TicketStatus): Int = transaction {
        TicketDbModel.update({ TicketDbModel.bookingId eq bookingId }) {
            it[TicketDbModel.status] = TicketStatusMapper.fromEnumToDbModel(status)
        }
    }
}