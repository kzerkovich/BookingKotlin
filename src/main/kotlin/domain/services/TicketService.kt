package domain.services

import domain.BookingStatus
import domain.TicketStatus
import domain.entities.Ticket
import domain.repository.BookingRepository
import domain.repository.EventRepository
import domain.repository.TicketRepository
import java.util.*

class TicketService(
    private val ticketRepository: TicketRepository,
    private val eventRepository: EventRepository,
    private val bookingRepository: BookingRepository
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

    fun purchaseTickets(bookingId: Int, paymentData: String): List<Ticket> {
        val booking = bookingRepository.getBooking(bookingId)
        if (booking.status != BookingStatus.PENDING) {
            throw IllegalStateException("Booking is not active")
        }


        val totalPrice = booking.ticketsCount * eventRepository.getEvent(booking.eventId).price
        val paymentSuccess = PaymentGateway.processPayment(paymentData, totalPrice)
        if (!paymentSuccess) {
            throw IllegalArgumentException("Payment failed")
        }

        val tickets = ticketRepository.getTicketsByBooking(bookingId)
        tickets.forEach { ticket ->
            ticket.status = TicketStatus.PURCHASED
            ticket.purchaseDate = Date()
            ticketRepository.editTicket(ticket)
        }

        return tickets
    }
}