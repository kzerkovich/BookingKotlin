package domain.usecases

import domain.TicketStatus
import domain.entities.Event
import domain.repository.EventRepository
import domain.repository.TicketRepository
import java.util.*

class EventUseCase(
    private val eventRepository: EventRepository,
    private val ticketRepository: TicketRepository
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
}