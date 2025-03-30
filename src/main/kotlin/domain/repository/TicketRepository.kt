package domain.repository

import domain.entities.Ticket

interface TicketRepository {
    fun addTicket(ticket: Ticket) : Ticket

    fun deleteTicket(ticketId: Int) : Int

    fun editTicket(ticket: Ticket) : Int

    fun getTicket(ticketId: Int): Ticket

    fun getAllTickets(): List<Ticket>
}