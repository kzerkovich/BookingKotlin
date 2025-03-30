package domain.repository

import domain.entities.Event

interface EventRepository {
    fun addEvent(event: Event) : Event

    fun deleteEvent(eventId: Int) : Int

    fun editEvent(event: Event) : Int

    fun getEvent(eventId: Int): Event

    fun getAllEvents(): List<Event>
}