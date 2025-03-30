package domain.usecases

import domain.BookingStatus
import domain.Roles
import domain.entities.Booking
import domain.repository.BookingRepository
import domain.repository.EventRepository
import domain.repository.UsersRepository
import java.nio.file.AccessDeniedException

class BookingUseCase(
    private val bookingRepository: BookingRepository,
    private val eventRepository: EventRepository,
    private val userRepository: UsersRepository
) {
    fun createBooking(booking: Booking): Booking {
        val event = eventRepository.getEvent(booking.eventId)
        if (event.availableTickets <= 0) {
            throw IllegalArgumentException("No tickets available for event ${event.id}")
        }
        return bookingRepository.addBooking(booking)
    }

    fun cancelBooking(bookingId: Int) {
        val booking = bookingRepository.getBooking(bookingId)
        if (booking.status == BookingStatus.CANCELLED) {
            throw IllegalStateException("Booking already cancelled")
        }
        bookingRepository.editBooking(booking.copy(status = BookingStatus.CANCELLED))
    }

    fun getBooking(bookingId: Int): Booking {
        return bookingRepository.getBooking(bookingId)
    }

    fun getBookingsByUser(userId: Int): List<Booking> {
        userRepository.getUser(userId)
        return bookingRepository.getAllBookings().filter { it.userId == userId }
    }

    fun updateBooking(updatedBooking: Booking): Booking {
        val existing = bookingRepository.getBooking(updatedBooking.id)
        if (existing.status == BookingStatus.CANCELLED) {
            throw IllegalStateException("Cannot update cancelled booking")
        }
        return bookingRepository.editBooking(updatedBooking).let { updatedBooking }
    }

    fun deleteBooking(bookingId: Int, requesterRole: Roles): Int {
        if (requesterRole != Roles.ADMIN) {
            throw AccessDeniedException("Only admins can delete bookings")
        }
        return bookingRepository.deleteBooking(bookingId)
    }
}