package data.services

import domain.Roles
import domain.TicketStatus
import domain.entities.Event
import domain.entities.Ticket
import domain.entities.User
import domain.repository.EventRepository
import domain.repository.TicketRepository
import domain.repository.UsersRepository
import domain.services.EventService
import domain.services.NotificationService
import domain.services.PaymentGateway
import io.mockk.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import java.util.*

class EventServiceTest {
    private val eventRepository = mockk<EventRepository>()
    private val ticketRepository = mockk<TicketRepository>()
    private val usersRepository = mockk<UsersRepository>()
    private val notificationService = mockk<NotificationService>()
    private lateinit var eventService: EventService

    @BeforeEach
    fun setup() {
        val testModule = module {
            single { eventRepository }
            single { ticketRepository }
            single { usersRepository }
            single { notificationService }
            single { EventService(get(), get(), get(), get()) }
        }
        startKoin { modules(testModule) }
        eventService = EventService(eventRepository, ticketRepository, notificationService, usersRepository)
    }

    @Test
    fun `cancelEvent should refund tickets and send notifications`() {

        val eventRepository = mockk<EventRepository>()
        val ticketRepository = mockk<TicketRepository>()
        val userRepository = mockk<UsersRepository>()

        val event = Event(
            id = 1,
            name = "Test Event",
            description = "Test",
            date = Date(),
            location = "Test",
            category = "Test",
            totalTickets = 100,
            availableTickets = 50,
            price = 100.0,
            isCancelled = false
        )
        val user = User(
            id = 1,
            login = "testUser",
            password = "testPass",
            email = "test@example.com",
            role = Roles.USER,
            bannedUntil = null
        )
        val ticket = Ticket(
            id = 1,
            eventId = 1,
            userId = 1,
            bookingId = null,
            status = TicketStatus.PURCHASED,
            purchaseDate = Date()
        )

        every { eventRepository.getEvent(1) } returns event
        every { ticketRepository.getAllTickets() } returns listOf(ticket)
        every { userRepository.getUser(1) } returns user
    }

    @AfterEach
    fun tearDown() {
        stopKoin()
    }
}