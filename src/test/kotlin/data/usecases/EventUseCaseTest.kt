package data.usecases

import domain.TicketStatus
import domain.entities.Event
import domain.entities.Ticket
import domain.repository.EventRepository
import domain.repository.TicketRepository
import domain.usecases.EventUseCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import java.util.*

class EventUseCaseTest {
    private val eventRepository = mockk<EventRepository>()
    private val ticketRepository = mockk<TicketRepository>()
    private lateinit var eventUseCase: EventUseCase

    @BeforeEach
    fun setup() {
        val testModule = module {
            single { eventRepository }
            single { ticketRepository }
            single { EventUseCase(get(), get()) }
        }
        startKoin { modules(testModule) }
        eventUseCase = EventUseCase(eventRepository, ticketRepository)
    }

    @Test
    fun `createEvent should throw error for past date`() {
        val pastDate = GregorianCalendar(2020, 0, 1).time
        val event = Event(
            id = 1,
            name = "Past Event",
            date = pastDate,
            location = "Test",
            category = "Test",
            availableTickets = 10,
            price = 100.0
        )

        assertThrows<IllegalArgumentException> {
            eventUseCase.createEvent(event)
        }
        verify(exactly = 0) { eventRepository.addEvent(any()) }
    }

    @Test
    fun `deleteEvent should throw error if tickets are booked`() {
        val eventId = 1

        val mockTicket = mockk<Ticket> {
            every { this@mockk.eventId } returns eventId
            every { status } returns TicketStatus.BOOKED
            every { id } returns 123
            every { userId } returns 456
        }

        every { ticketRepository.getAllTickets() } returns listOf(mockTicket)

        assertThrows<IllegalStateException> {
            eventUseCase.deleteEvent(eventId)
        }

        verify(exactly = 1) { ticketRepository.getAllTickets() }
    }

    @AfterEach
    fun tearDown() {
        stopKoin()
    }
}