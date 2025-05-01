package domain.repository

import domain.entities.Event
import java.util.*

interface EventRepository {
    fun addEvent(event: Event) : Event

    fun deleteEvent(eventId: Int) : Int

    fun editEvent(event: Event) : Int

    fun getEvent(eventId: Int): Event

    fun getAllEvents(): List<Event>

    fun getEventsByFilters(
        location: String? = null,
        category: String? = null,
        startDate: Date? = null,
        endDate: Date? = null
    ): List<Event>

    fun cancelEvent(eventId: Int): Int
}