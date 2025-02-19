package data.repositoryImpl

import domain.entities.Ticket
import domain.repository.TicketRepository

class TicketRepositoryImpl(): TicketRepository {
    var ticketList = mutableListOf<Ticket>()
    private var autoIncrementId = 0

    override fun addTicket(ticket: Ticket) {
        if (ticket.id == Ticket.UNDEFINED_ID)
            ticket.id = autoIncrementId++
        ticketList.add(ticket)
    }

    override fun deleteTicket(ticket: Ticket) {
        val id = ticketList.indexOf(ticket)
        ticketList.removeAt(id)
    }

    override fun editTicket(ticket: Ticket) {
        val oldTicket = getTicket(ticket.id)
        ticketList.remove(oldTicket)
        addTicket(ticket)
    }

    override fun getTicket(ticketId: Int): Ticket {
        return ticketList.find {
            it.id == ticketId
        } ?: throw RuntimeException("Element with id = $ticketId not found")
    }

    override fun getAllTickets(): List<Ticket> {
        return ticketList
    }
}