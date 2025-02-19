package data.repositoryImpl

import domain.entities.Event
import domain.repository.EventRepository

class EventRepositoryImpl(): EventRepository {
    var eventList = mutableListOf<Event>()
    private var autoIncrementId = 0

    override fun addEvent(event: Event) {
        if (event.id == Event.UNDEFINED_ID)
            event.id = autoIncrementId++
        eventList.add(event)
    }

    override fun deleteEvent(event: Event) {
        val id = eventList.indexOf(event)
        eventList.removeAt(id)
    }

    override fun editEvent(event: Event) {
        val oldEvent = getEvent(event.id)
        eventList.remove(oldEvent)
        addEvent(event)
    }

    override fun getEvent(eventId: Int): Event {
        return eventList.find {
            it.id == eventId
        } ?: throw RuntimeException("Element with id = $eventId not found")
    }

    override fun getAllEvents(): List<Event> {
        return eventList
    }

}