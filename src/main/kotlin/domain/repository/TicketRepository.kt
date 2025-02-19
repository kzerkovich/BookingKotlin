package domain.repository

import domain.entities.Ticket

interface TicketRepository {
    fun addTicket(ticket: Ticket)

    fun deleteTicket(ticket: Ticket)

    fun editTicket(ticket: Ticket)

    fun getTicket(ticketId: Int): Ticket

    fun getAllTickets(): List<Ticket>
}