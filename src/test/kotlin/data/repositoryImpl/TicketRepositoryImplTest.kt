package data.repositoryImpl

import BaseCrudTest
import domain.Roles
import domain.TicketStatus
import domain.entities.Event
import domain.entities.Ticket
import domain.entities.User
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*

class TicketCrudTest : BaseCrudTest() {
    private val ticketRepo = TicketRepositoryImpl()
    private val eventRepo = EventRepositoryImpl()
    private val userRepo = UsersRepositoryImpl()

    private fun createTestUser(): User {
        return User(
            id = 0,
            login = "test_user_${UUID.randomUUID()}",
            password = "SecurePass123!",
            email = "test@example.com",
            role = Roles.USER
        )
    }

    private fun createEvent(): Event {
        return eventRepo.addEvent(
            Event(
                id = 0,
                name = "Rock Festival",
                date = Date(System.currentTimeMillis() + 86400000), // Завтра
                location = "Stadium",
                category = "Music",
                availableTickets = 500,
                price = 99.99
            )
        )
    }

    @Test
    fun `create, read and delete ticket`() {
        val event = createEvent()

        val user = userRepo.addUser(createTestUser())

        val ticket = ticketRepo.addTicket(
            Ticket(
                id = 0,
                eventId = event.id,
                userId = user.id,
                status = TicketStatus.AVAILABLE
            )
        )

        val foundTicket = ticketRepo.getTicket(ticket.id)
        assertAll(
            { assertEquals(event.id, foundTicket.eventId) },
            { assertEquals(TicketStatus.AVAILABLE, foundTicket.status) },
            { assertEquals(foundTicket.userId, user.id) }
        )

        ticketRepo.deleteTicket(ticket.id)
        assertThrows<NoSuchElementException> {
            ticketRepo.getTicket(ticket.id)
        }
    }

    @Test
    fun `update ticket status and user`() {
        val event = createEvent()
        val user = userRepo.addUser(createTestUser())

        val ticket = ticketRepo.addTicket(
            Ticket(
                id = 0,
                eventId = event.id,
                userId = user.id,
                status = TicketStatus.AVAILABLE
            )
        )

        val updatedTicket = ticket.copy(
            status = TicketStatus.BOOKED,
            userId = user.id
        )
        ticketRepo.editTicket(updatedTicket)

        val foundTicket = ticketRepo.getTicket(ticket.id)
        assertAll(
            { assertEquals(TicketStatus.BOOKED, foundTicket.status) },
            { assertEquals(user.id, foundTicket.userId) }
        )
    }

    @Test
    fun `fail to create ticket for non-existing event`() {
        assertThrows<Exception> {
            ticketRepo.addTicket(
                Ticket(
                    id = 0,
                    eventId = 999,
                    userId = 0,
                    status = TicketStatus.AVAILABLE
                )
            )
        }
    }
}