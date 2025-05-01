package data.repositoryImpl

import domain.entities.Event
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*

class EventCrudTest : BaseCrudTest() {
    private val eventRepo = EventRepositoryImpl()

    private fun createTestEvent(): Event {
        return Event(
            id = 0,
            name = "Test Event ${UUID.randomUUID()}",
            description = "Test Description",
            date = Date(System.currentTimeMillis() + 86400000),
            location = "Test Location",
            category = "Music",
            totalTickets = 200,
            availableTickets = 100,
            price = 50.00,
            isCancelled = false
        )
    }

    @Test
    fun `create and read event`() {
        val event = createTestEvent()

        val savedEvent = eventRepo.addEvent(event)
        val foundEvent = eventRepo.getEvent(savedEvent.id)

        assertAll(
            { assertEquals(savedEvent.id, foundEvent.id) },
            { assertEquals(event.name, foundEvent.name) },
            { assertEquals(event.location, foundEvent.location) },
            { assertEquals(100, foundEvent.availableTickets) }
        )
    }

    @Test
    fun `update event tickets and price`() {
        val event = eventRepo.addEvent(createTestEvent())
        val updatedEvent = event.copy(
            availableTickets = 50,
            price = 75.50
        )

        eventRepo.editEvent(updatedEvent)
        val result = eventRepo.getEvent(event.id)

        assertAll(
            { assertEquals(50, result.availableTickets) },
            { assertEquals(75.50, result.price) }
        )
    }

    @Test
    fun `delete event`() {
        val event = eventRepo.addEvent(createTestEvent())

        eventRepo.deleteEvent(event.id)

        assertThrows<NoSuchElementException> {
            eventRepo.getEvent(event.id)
        }
    }

    @Test
    fun `cancel event`() {
        val event = eventRepo.addEvent(createTestEvent())
        eventRepo.cancelEvent(event.id)
        val result = eventRepo.getEvent(event.id)
        assertEquals(true, result.isCancelled)
    }
}