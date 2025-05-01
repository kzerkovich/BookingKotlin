package domain.services

import domain.BookingStatus
import domain.entities.Booking
import domain.repository.BookingRepository
import domain.repository.EventRepository
import domain.repository.UsersRepository
import java.nio.file.AccessDeniedException
import java.util.*

class BookingService(
    private val bookingRepository: BookingRepository,
    private val eventRepository: EventRepository,
    private val userRepository: UsersRepository
) {
    fun createBooking(userId: Int, eventId: Int, ticketsCount: Int): Booking {
        val user = userRepository.getUser(userId)
        val event = eventRepository.getEvent(eventId)


        if (user.bannedUntil?.after(Date()) == true) {
            throw AccessDeniedException("User is banned until ${user.bannedUntil}")
        }

        if (event.availableTickets < ticketsCount) {
            throw IllegalArgumentException("Not enough tickets available")
        }

        val activeBookings = bookingRepository.findActiveBookingsByUserAndEvent(userId, eventId)
        if (activeBookings.any { it.status == BookingStatus.PENDING }) {
            throw IllegalStateException("User already has an active booking for this event")
        }

        if (ticketsCount > event.totalTickets / 3) {
            user.bannedUntil = Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000)
            userRepository.editUser(user)
            throw IllegalArgumentException("Cannot book more than 1/3 of tickets")
        }

        val expirationDate = Date(System.currentTimeMillis() + 30 * 60 * 1000)
        val booking = Booking(
            id = 0,
            eventId = eventId,
            userId = userId,
            ticketsCount = ticketsCount,
            bookingDate = Date(),
            expirationDate = expirationDate,
            status = BookingStatus.PENDING
        )

        event.availableTickets -= ticketsCount
        eventRepository.editEvent(event)

        return bookingRepository.addBooking(booking)
    }

    fun confirmBooking(bookingId: Int, paymentData: String): Booking {
        val booking = bookingRepository.getBooking(bookingId)


        if (booking.expirationDate.before(Date())) {
            throw IllegalStateException("Booking expired")
        }
        val totalPrice = booking.ticketsCount * eventRepository.getEvent(booking.eventId).price
        val paymentSuccess = PaymentGateway.processPayment(paymentData, totalPrice)
        if (!paymentSuccess) {
            throw IllegalArgumentException("Payment failed")
        }

        val updatedBooking = booking.copy(status = BookingStatus.CONFIRMED)
        bookingRepository.editBooking(updatedBooking)
        return updatedBooking
    }

    fun cancelBooking(bookingId: Int) {
        val booking = bookingRepository.getBooking(bookingId)
        if (booking.status != BookingStatus.PENDING) {
            throw IllegalStateException("Only pending bookings can be cancelled")
        }

        val event = eventRepository.getEvent(booking.eventId)
        event.availableTickets += booking.ticketsCount
        eventRepository.editEvent(event)

        bookingRepository.editBooking(booking.copy(status = BookingStatus.CANCELLED))
    }

    fun getUserBookings(userId: Int): List<Booking> {
        return bookingRepository.getAllBookings()
            .filter { it.userId == userId && it.status != BookingStatus.CANCELLED }
    }

    fun checkExpiredBookings() {
        val expiredBookings = bookingRepository.findAllByStatus(BookingStatus.PENDING)
            .filter { it.expirationDate.before(Date()) }

        expiredBookings.forEach { booking ->
            cancelBooking(booking.id)
        }
    }
}