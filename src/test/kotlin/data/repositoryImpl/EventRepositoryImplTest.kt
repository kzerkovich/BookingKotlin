package data.repositoryImpl

import domain.entities.Event
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals

class EventRepositoryImplTest {
    private val testEventRepositoryImpl: EventRepositoryImpl = EventRepositoryImpl()

    private val eventTest1 = Event(
        id = -1,
        name = "testEvent",
        date = Date(System.currentTimeMillis()),
        location = "testLocation",
        category = "testCategory",
        availableTickets = AVAILABLE_TICKETS_NUM,
        price = PRICE
    )

    private val eventTest2 = Event(
        id = -1,
        name = "testEvent",
        date = Date(System.currentTimeMillis()),
        location = "testLocation",
        category = "testCategory",
        availableTickets = AVAILABLE_TICKETS_NUM,
        price = PRICE
    )

    private val eventTest3 = Event(
        id = 10,
        name = "testEvent",
        date = Date(System.currentTimeMillis()),
        location = "testLocation",
        category = "testCategory",
        availableTickets = AVAILABLE_TICKETS_NUM,
        price = PRICE
    )

    private val eventTestEdited = Event(
        id = 10,
        name = "testEvent",
        date = Date(System.currentTimeMillis()),
        location = "newLocation",
        category = "testCategory",
        availableTickets = AVAILABLE_TICKETS_NUM,
        price = PRICE
    )

    @Test
    @DisplayName("addEvent test")
    fun addEvent() {
        testEventRepositoryImpl.addEvent(eventTest1)
        testEventRepositoryImpl.addEvent(eventTest2)
        testEventRepositoryImpl.addEvent(eventTest3)

        assertEquals(3, testEventRepositoryImpl.eventList.size)
    }

    @Test
    @DisplayName("deleteEvent test")
    fun deleteEvent() {
        testEventRepositoryImpl.addEvent(eventTest1)
        testEventRepositoryImpl.addEvent(eventTest2)
        testEventRepositoryImpl.addEvent(eventTest3)


        testEventRepositoryImpl.deleteEvent(eventTest1)
        assertEquals(2, testEventRepositoryImpl.eventList.size)

        testEventRepositoryImpl.deleteEvent(eventTest2)
        assertEquals(1, testEventRepositoryImpl.eventList.size)

        testEventRepositoryImpl.deleteEvent(eventTest3)
        assertEquals(0, testEventRepositoryImpl.eventList.size)
    }

    @Test
    @DisplayName("editEvent test")
    fun editEvent() {
        testEventRepositoryImpl.addEvent(eventTest1)
        testEventRepositoryImpl.addEvent(eventTest2)
        testEventRepositoryImpl.addEvent(eventTest3)

        testEventRepositoryImpl.editEvent(eventTestEdited)
        assertEquals(testEventRepositoryImpl.getEvent(10).location, "newLocation")
    }

    @Test
    @DisplayName("getEvent test")
    fun getEvent() {
        testEventRepositoryImpl.addEvent(eventTest1)
        testEventRepositoryImpl.addEvent(eventTest2)
        testEventRepositoryImpl.addEvent(eventTest3)

        assertEquals(eventTest1, testEventRepositoryImpl.getEvent(0))
        assertEquals(eventTest2, testEventRepositoryImpl.getEvent(1))
        assertEquals(eventTest3, testEventRepositoryImpl.getEvent(10))
    }

    @Test
    @DisplayName("getAllEvents test")
    fun getAllEvents() {
        testEventRepositoryImpl.addEvent(eventTest1)
        testEventRepositoryImpl.addEvent(eventTest2)
        testEventRepositoryImpl.addEvent(eventTest3)

        assertEquals(mutableListOf(eventTest1, eventTest2, eventTest3), testEventRepositoryImpl.getAllEvents())
    }
    
    companion object {
        private const val AVAILABLE_TICKETS_NUM = 1000
        private const val PRICE = 1000
    }
}