package data.repositoryImpl

import domain.BookingStatus
import domain.Roles
import domain.entities.Booking
import domain.entities.Event
import domain.entities.User
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*

class BookingCrudTest : BaseCrudTest() {
    private val bookingRepo = BookingRepositoryImpl()
    private val eventRepo = EventRepositoryImpl()
    private val userRepo = UsersRepositoryImpl()

    private fun createEvent(): Event {
        return eventRepo.addEvent(
            Event(
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
        )
    }

    private fun createUser(): User {
        return userRepo.addUser(
            User(
                id = 0,
                login = "user_${UUID.randomUUID()}",
                password = "SecurePass!123",
                email = "booking_test@example.com",
                role = Roles.USER
            )
        )
    }

    @Test
    fun `create, read and delete booking`() {
        val event = createEvent()
        val user = createUser()

        val booking = bookingRepo.addBooking(
            Booking(
                id = 0,
                eventId = event.id,
                userId = user.id,
                ticketsCount = 2,
                bookingDate = Date(),
                expirationDate = Date(System.currentTimeMillis() + 30 * 60 * 1000),
                status = BookingStatus.PENDING
            )
        )
        val foundBooking = bookingRepo.getBooking(booking.id)
        assertAll(
            { assertEquals(event.id, foundBooking.eventId) },
            { assertEquals(user.id, foundBooking.userId) },
            { assertEquals(BookingStatus.PENDING, foundBooking.status) }
        )

        bookingRepo.deleteBooking(booking.id)
        assertThrows<NoSuchElementException> {
            bookingRepo.getBooking(booking.id)
        }
    }

    @Test
    fun `update booking status`() {
        val booking = bookingRepo.addBooking(
            Booking(
                id = 0,
                eventId = createEvent().id,
                userId = createUser().id,
                ticketsCount = 1,
                bookingDate = Date(),
                expirationDate = Date(System.currentTimeMillis() + 30 * 60 * 1000), // Добавлено
                status = BookingStatus.PENDING
            )
        )
        val updatedBooking = booking.copy(status = BookingStatus.CANCELLED)
        bookingRepo.editBooking(updatedBooking)

        assertEquals(BookingStatus.CANCELLED, bookingRepo.getBooking(booking.id).status)
    }

    @Test
    fun `fail to create booking with invalid user`() {
        assertThrows<Exception> {
            bookingRepo.addBooking(
                Booking(
                    id = 0,
                    eventId = createEvent().id,
                    userId = 0,
                    ticketsCount = 999,
                    bookingDate = Date(),
                    expirationDate = Date(System.currentTimeMillis() + 30 * 60 * 1000),
                    status = BookingStatus.PENDING
                )
            )
        }
    }

    @Test
    fun `fail to create booking with invalid event`() {
        assertThrows<Exception> {
            bookingRepo.addBooking(
                Booking(
                    id = 0,
                    eventId = 999,
                    userId = createUser().id,
                    ticketsCount = 1,
                    bookingDate = Date(),
                    expirationDate = Date(System.currentTimeMillis() + 30 * 60 * 1000), // Добавлено
                    status = BookingStatus.PENDING
                )
            )
        }
    }
}