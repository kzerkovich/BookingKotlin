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
        println("Creating new event: ${event.name}")
        if (event.date.before(Date())) {
            println("Invalid event date: ${event.date}")
            throw IllegalArgumentException("Event date cannot be in the past")
        }
        val createdEvent = eventRepository.addEvent(event)
        println("Event ${createdEvent.id} created successfully")
        return createdEvent
    }

    fun updateEvent(event: Event): Event {
        println("Updating event ${event.id}")
        if (event.date.before(Date())) {
            println("Attempt to set past date for event ${event.id}")
            throw IllegalArgumentException("Event date cannot be in the past")
        }
        eventRepository.editEvent(event)
        println("Event ${event.id} updated: $event")
        return event
    }

    fun deleteEvent(eventId: Int): Int {
        println("Deleting event $eventId")
        val tickets = ticketRepository.getAllTickets().filter { it.eventId == eventId }
        if (tickets.any { it.status == TicketStatus.BOOKED }) {
            println("Delete blocked - active bookings for event $eventId")
            throw IllegalStateException("Cannot delete event with active bookings")
        }
        val result = eventRepository.deleteEvent(eventId)
        println("Event $eventId deleted. Tickets removed: ${tickets.size}")
        return result
    }

    fun getEvent(eventId: Int): Event {
        println("Fetching event by ID: $eventId")
        val event = eventRepository.getEvent(eventId)
        println("Retrieved event: ${event.id} - ${event.name}")
        return event
    }

    fun getEventsByLocation(location: String): List<Event> {
        println("Searching events by location: $location")
        val events = eventRepository.getAllEvents().filter { it.location == location }

        when {
            events.isEmpty() -> println("No events found for location: $location")
            else -> println("Found ${events.size} events in $location")
        }
        return events
    }

    fun getAllEvents(): List<Event> {
        println("Fetching all events")
        val events = eventRepository.getAllEvents()
        println("Total events in system: ${events.size}")
        return events
    }

    fun getFilteredEvents(
        location: String? = null,
        category: String? = null,
        startDate: Date? = null,
        endDate: Date? = null
    ): List<Event> {
        println(
            "Filtering events with params: " +
                    "location=$location, category=$category, " +
                    "startDate=${startDate?.toInstant()}, endDate=${endDate?.toInstant()}"
        )
        val events = eventRepository.getEventsByFilters(location, category, startDate, endDate)

        when {
            events.isEmpty() -> println("No events match the filters")
            else -> println("Found ${events.size} events matching criteria")
        }
        return events
    }

    fun updateEventTickets(eventId: Int, newTotal: Int, requesterRole: Roles): Event {
        println("Updating tickets for event $eventId (role: $requesterRole)")
        if (requesterRole != Roles.ADMIN) {
            println("Unauthorized ticket update attempt by $requesterRole")
            throw AccessDeniedException("Only admins can update tickets")
        }
        val event = eventRepository.getEvent(eventId)
        event.availableTickets += (newTotal - event.totalTickets)
        event.totalTickets = newTotal
        eventRepository.editEvent(event)

        println("Event $eventId tickets updated. New total: $newTotal")
        return event
    }

    fun updateEventPrice(eventId: Int, newPrice: Double, requesterRole: Roles): Event {
        println("Updating price for event $eventId (role: $requesterRole)")
        if (requesterRole != Roles.ADMIN) {
            println("Unauthorized price update attempt by $requesterRole")
            throw AccessDeniedException("Only admins can update price")
        }
        val event = eventRepository.getEvent(eventId)
        event.price = newPrice
        eventRepository.editEvent(event)

        println("Event $eventId price updated to $$$newPrice")
        return event
    }


    fun cancelEvent(eventId: Int, requesterRole: Roles): Int {
        println("Cancelling event $eventId (role: $requesterRole)")

        if (requesterRole != Roles.ADMIN) {
            println("Unauthorized cancel attempt by $requesterRole")
            throw AccessDeniedException("Only admins can cancel events")
        }
        val event = eventRepository.getEvent(eventId)
        event.isCancelled = true

        val tickets = ticketRepository.getAllTickets().filter { it.eventId == eventId }

        println("Processing refunds for ${tickets.size} tickets")

        tickets.filter { it.status == TicketStatus.PURCHASED }
            .forEach { ticket ->
                println("Refunding user ${ticket.userId} for ticket ${ticket.id}")
                PaymentGateway.refundPayment(ticket.userId, event.price)
                val user = userRepository.getUser(ticket.userId)
                if (user.notificationEnabled) {
                    println("Sending notification to user ${user.id}")
                    notificationService.notifyUser(user, "Event '${event.name}' cancelled. Refund processed.")
                }
            }

        val result = eventRepository.cancelEvent(eventId)
        println("Event $eventId cancelled. Refunds processed: ${tickets.size}")
        return result
    }
}