package domain.repository

import domain.entities.Event

interface EventRepository {
    fun addEvent(event: Event)

    fun deleteEvent(event: Event)

    fun editEvent(event: Event)

    fun getEvent(eventId: Int): Event

    fun getAllEvents(): List<Event>
}