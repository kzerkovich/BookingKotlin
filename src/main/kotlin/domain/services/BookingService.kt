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
        println("Creating booking: userId=$userId, eventId=$eventId, tickets=$ticketsCount")
        val user = userRepository.getUser(userId)
        val event = eventRepository.getEvent(eventId)


        if (user.bannedUntil?.after(Date()) == true) {
            println("User $userId banned until ${user.bannedUntil}. Booking rejected")
            throw AccessDeniedException("User is banned until ${user.bannedUntil}")
        }

        if (event.availableTickets < ticketsCount) {
            println("Not enough tickets (requested: $ticketsCount, available: ${event.availableTickets})")
            throw IllegalArgumentException("Not enough tickets available")
        }

        val activeBookings = bookingRepository.findActiveBookingsByUserAndEvent(userId, eventId)
        if (activeBookings.any { it.status == BookingStatus.PENDING }) {
            println("Duplicate booking attempt for user $userId")
            throw IllegalStateException("User already has an active booking for this event")
        }

        if (ticketsCount > event.totalTickets / 3) {
            println("User $userId exceeded ticket limit (${event.totalTickets / 3})")
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

        println("Booking ${booking.id} created. Tickets left: ${event.availableTickets}")
        return bookingRepository.addBooking(booking)
    }

    fun confirmBooking(bookingId: Int, paymentData: String): Booking {
        println("Confirming booking $bookingId")
        val booking = bookingRepository.getBooking(bookingId)

        if (booking.expirationDate.before(Date())) {
            println("Booking $bookingId expired at ${booking.expirationDate}")
            throw IllegalStateException("Booking expired")
        }
        val totalPrice = booking.ticketsCount * eventRepository.getEvent(booking.eventId).price
        println("Processing payment: $${totalPrice} for booking $bookingId")

        val paymentSuccess = PaymentGateway.processPayment(paymentData, totalPrice)
        if (!paymentSuccess) {
            println("Payment failed for booking $bookingId")
            throw IllegalArgumentException("Payment failed")
        }

        val updatedBooking = booking.copy(status = BookingStatus.CONFIRMED)
        bookingRepository.editBooking(updatedBooking)

        println("Booking $bookingId confirmed successfully")
        return updatedBooking
    }

    fun cancelBooking(bookingId: Int) {
        println("Cancelling booking $bookingId")
        val booking = bookingRepository.getBooking(bookingId)
        if (booking.status != BookingStatus.PENDING) {
            println("Invalid cancel attempt for booking $bookingId (status: ${booking.status})")
            throw IllegalStateException("Only pending bookings can be cancelled")
        }

        val event = eventRepository.getEvent(booking.eventId)
        event.availableTickets += booking.ticketsCount
        eventRepository.editEvent(event)

        println("Booking $bookingId cancelled. Tickets restored: ${booking.ticketsCount}")
        bookingRepository.editBooking(booking.copy(status = BookingStatus.CANCELLED))
    }

    fun getUserBookings(userId: Int): List<Booking> {
        println("Get bookings for user with id = $userId")
        return bookingRepository.getAllBookings()
            .filter { it.userId == userId && it.status != BookingStatus.CANCELLED }
    }

    fun checkExpiredBookings() {
        println("Checking for expired bookings")
        val expiredBookings = bookingRepository.findAllByStatus(BookingStatus.PENDING)
            .filter { it.expirationDate.before(Date()) }

        println("Found ${expiredBookings.size} expired bookings")
        expiredBookings.forEach { booking ->
            println("Auto-cancelling booking ${booking.id}")
            cancelBooking(booking.id)
        }
    }
}