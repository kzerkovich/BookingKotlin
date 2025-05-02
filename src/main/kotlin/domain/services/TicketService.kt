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
        println("Creating ticket for event ${ticket.eventId}")
        val event = eventRepository.getEvent(ticket.eventId)
        if (event.availableTickets <= 0) {
            println("Ticket creation failed: No tickets left for event ${event.id}")
            throw IllegalArgumentException("No tickets left for event ${event.id}")
        }
        val createdTicket = ticketRepository.addTicket(ticket)
        println("Ticket ${createdTicket.id} created successfully")
        return createdTicket
    }

    fun updateTicketStatus(ticketId: Int, status: TicketStatus): Ticket {
        println("Updating status for ticket $ticketId to $status")
        val ticket = ticketRepository.getTicket(ticketId)
        val updatedTicket = ticket.copy(status = status)
        ticketRepository.editTicket(updatedTicket)
        println("Ticket $ticketId status updated")
        return updatedTicket
    }

    fun getTicketsByEvent(eventId: Int): List<Ticket> {
        println("Fetching tickets for event $eventId")
        val tickets = ticketRepository.getAllTickets().filter { it.eventId == eventId }
        println("Found ${tickets.size} tickets for event $eventId")
        return tickets
    }

    fun deleteTicket(ticketId: Int): Int {
        println("Deleting ticket $ticketId")
        val result = ticketRepository.deleteTicket(ticketId)
        println("Ticket $ticketId deleted (result code: $result)")
        return result
    }

    fun purchaseTickets(bookingId: Int, paymentData: String): List<Ticket> {
        println("Processing purchase for booking $bookingId")
        val booking = bookingRepository.getBooking(bookingId)
        if (booking.status != BookingStatus.PENDING) {
            println("Purchase failed: Booking $bookingId is not active")
            throw IllegalStateException("Booking is not active")
        }

        val totalPrice = booking.ticketsCount * eventRepository.getEvent(booking.eventId).price
        println("Total price: $totalPrice, Payment data: $paymentData")
        val paymentSuccess = PaymentGateway.processPayment(paymentData, totalPrice)
        if (!paymentSuccess) {
            println("Payment failed for booking $bookingId")
            throw IllegalArgumentException("Payment failed")
        }

        val tickets = ticketRepository.getTicketsByBooking(bookingId)
        tickets.forEach { ticket ->
            ticket.status = TicketStatus.PURCHASED
            ticket.purchaseDate = Date()
            ticketRepository.editTicket(ticket)
            println("Ticket ${ticket.id} marked as purchased")
        }

        println("${tickets.size} tickets purchased for booking $bookingId")
        return tickets
    }
}