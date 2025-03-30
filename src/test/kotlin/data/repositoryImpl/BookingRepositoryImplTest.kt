package data.repositoryImpl

import BaseCrudTest
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
                name = "Tech Conference",
                date = Date(System.currentTimeMillis() + 172800000),
                location = "Convention Center",
                category = "Technology",
                availableTickets = 200,
                price = 299.99
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
                bookingDate = Date(),
                status = BookingStatus.CONFIRMED
            )
        )

        val foundBooking = bookingRepo.getBooking(booking.id)
        assertAll(
            { assertEquals(event.id, foundBooking.eventId) },
            { assertEquals(user.id, foundBooking.userId) },
            { assertEquals(BookingStatus.CONFIRMED, foundBooking.status) }
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
                bookingDate = Date(),
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
                    userId = 999,
                    bookingDate = Date(),
                    status = BookingStatus.CONFIRMED
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
                    bookingDate = Date(),
                    status = BookingStatus.CONFIRMED
                )
            )
        }
    }
}