package domain.usecases

import domain.TicketStatus
import domain.entities.Ticket
import domain.repository.EventRepository
import domain.repository.TicketRepository

class TicketUseCase(
    private val ticketRepository: TicketRepository,
    private val eventRepository: EventRepository
) {
    fun createTicket(ticket: Ticket): Ticket {
        val event = eventRepository.getEvent(ticket.eventId)
        if (event.availableTickets <= 0) {
            throw IllegalArgumentException("No tickets left for event ${event.id}")
        }
        return ticketRepository.addTicket(ticket)
    }

    fun updateTicketStatus(ticketId: Int, status: TicketStatus): Ticket {
        val ticket = ticketRepository.getTicket(ticketId)
        return ticketRepository.editTicket(ticket.copy(status = status)).let { ticket }
    }

    fun getTicketsByEvent(eventId: Int): List<Ticket> {
        return ticketRepository.getAllTickets().filter { it.eventId == eventId }
    }

    fun deleteTicket(ticketId: Int): Int = ticketRepository.deleteTicket(ticketId)
}