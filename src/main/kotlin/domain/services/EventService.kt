package domain.services

import domain.Roles
import domain.TicketStatus
import domain.entities.Event
import domain.repository.EventRepository
import domain.repository.TicketRepository
import domain.repository.UsersRepository
import java.nio.file.AccessDeniedException
import java.util.*

class EventService(
    private val eventRepository: EventRepository,
    private val ticketRepository: TicketRepository,
    private val notificationService: NotificationService,
    private val userRepository: UsersRepository
) {
    fun createEvent(event: Event): Event {
        if (event.date.before(Date())) {
            throw IllegalArgumentException("Event date cannot be in the past")
        }
        return eventRepository.addEvent(event)
    }

    fun updateEvent(event: Event): Event {
        if (event.date.before(Date())) {
            throw IllegalArgumentException("Event date cannot be in the past")
        }
        eventRepository.editEvent(event)
        return event
    }

    fun deleteEvent(eventId: Int): Int {
        val tickets = ticketRepository.getAllTickets().filter { it.eventId == eventId }
        if (tickets.any { it.status == TicketStatus.BOOKED }) {
            throw IllegalStateException("Cannot delete event with active bookings")
        }
        return eventRepository.deleteEvent(eventId)
    }

    fun getEvent(eventId: Int): Event = eventRepository.getEvent(eventId)

    fun getEventsByLocation(location: String): List<Event> {
        return eventRepository.getAllEvents().filter { it.location == location }
    }

    fun getFilteredEvents(
        location: String? = null,
        category: String? = null,
        startDate: Date? = null,
        endDate: Date? = null
    ): List<Event> {
        return eventRepository.getEventsByFilters(location, category, startDate, endDate)
    }

    fun updateEventTickets(eventId: Int, newTotal: Int, requesterRole: Roles): Event {
        if (requesterRole != Roles.ADMIN) {
            throw AccessDeniedException("Only admins can update tickets")
        }
        val event = eventRepository.getEvent(eventId)
        event.availableTickets += (newTotal - event.totalTickets)
        event.totalTickets = newTotal
        eventRepository.editEvent(event)
        return event
    }

    fun updateEventPrice(eventId: Int, newPrice: Double, requesterRole: Roles): Event {
        if (requesterRole != Roles.ADMIN) {
            throw AccessDeniedException("Only admins can update price")
        }
        val event = eventRepository.getEvent(eventId)
        event.price = newPrice
        eventRepository.editEvent(event)
        return event
    }


    fun cancelEvent(eventId: Int, requesterRole: Roles): Int {
        if (requesterRole != Roles.ADMIN) {
            throw AccessDeniedException("Only admins can cancel events")
        }
        val event = eventRepository.getEvent(eventId)
        event.isCancelled = true

        val tickets = ticketRepository.getAllTickets().filter { it.eventId == eventId }
        tickets.filter { it.status == TicketStatus.PURCHASED }
            .forEach { ticket ->
                PaymentGateway.refundPayment(ticket.userId, event.price)
                val user = userRepository.getUser(ticket.userId)
                if (user.notificationEnabled) {
                    notificationService.notifyUser(user, "Event '${event.name}' cancelled. Refund processed.")
                }
            }

        return eventRepository.cancelEvent(eventId)
    }
}