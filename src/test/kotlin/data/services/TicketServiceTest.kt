package data.services

import domain.entities.Ticket
import domain.TicketStatus
import domain.entities.Event
import domain.repository.BookingRepository
import domain.repository.EventRepository
import domain.repository.TicketRepository
import domain.services.TicketService
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

class TicketServiceTest {
    private val ticketRepository = mockk<TicketRepository>()
    private val eventRepository = mockk<EventRepository>()
    private val bookingRepository = mockk<BookingRepository>()
    private lateinit var ticketService: TicketService

    @BeforeEach
    fun setup() {
        val testModule = module {
            single { ticketRepository }
            single { eventRepository }
            single { TicketService(get(), get(), get()) }
        }
        startKoin { modules(testModule) }
        ticketService = TicketService(ticketRepository, eventRepository, bookingRepository)
    }

    @Test
    fun `createTicket should throw error if no tickets left`() {
        val eventId = 1

        val mockEvent = Event(
            id = eventId,
            name = "Test Event",
            description = "Test",
            date = Date(),
            location = "Test Location",
            category = "Test Category",
            availableTickets = 0,
            price = 100.0,
            totalTickets = 100
        )

        every { eventRepository.getEvent(eventId) } returns mockEvent

        assertThrows<IllegalArgumentException> {
            ticketService.createTicket(
                Ticket(
                    id = 1,
                    eventId = eventId,
                    userId = 1
                )
            )
        }

        verify(exactly = 0) { ticketRepository.addTicket(any()) }
    }

    @Test
    fun `updateTicketStatus should change status`() {
        val ticketId = 1
        every { ticketRepository.getTicket(ticketId) } returns Ticket(
            id = ticketId,
            eventId = 123,
            userId = 123,
            status = TicketStatus.AVAILABLE
        )
        every { ticketRepository.editTicket(any()) } returns 1

        ticketService.updateTicketStatus(ticketId, TicketStatus.PURCHASED)

        verify { ticketRepository.editTicket(match { it.status == TicketStatus.PURCHASED }) }
    }

    @AfterEach
    fun tearDown() {
        stopKoin()
    }
}