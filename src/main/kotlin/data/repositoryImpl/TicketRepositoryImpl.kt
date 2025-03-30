package data.repositoryImpl

import data.db.entities.TicketDbModel
import data.mappers.TicketMapper
import data.mappers.enums.TicketStatusMapper
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
            it[status] = TicketStatusMapper.fromEnumToDbModel(ticket.status)
        }.value

        ticket.copy(id = id)
    }

    override fun deleteTicket(ticketId: Int) = transaction {
        TicketDbModel.deleteWhere { TicketDbModel.id eq ticketId }
    }

    override fun editTicket(ticket: Ticket) = transaction {
        TicketDbModel.update({ TicketDbModel.id eq ticket.id }) {
            it[eventId] = ticket.eventId
            it[userId] = ticket.userId
            it[status] = TicketStatusMapper.fromEnumToDbModel(ticket.status)
        }
    }

    override fun getTicket(ticketId: Int): Ticket = transaction {
        TicketDbModel.select { TicketDbModel.id eq ticketId }
            .map { row ->
                TicketMapper.fromDbModelToEntity(row)
            }.firstOrNull() ?: throw NoSuchElementException("Ticket $ticketId not found")
    }

    override fun getAllTickets(): List<Ticket> = transaction {
        TicketDbModel.selectAll().map { row ->
            TicketMapper.fromDbModelToEntity(row)
        }
    }
}