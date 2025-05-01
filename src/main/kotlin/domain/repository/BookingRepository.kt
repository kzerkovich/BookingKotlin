package domain.repository

import domain.BookingStatus
import domain.entities.Booking

interface BookingRepository {
    fun addBooking(booking: Booking) : Booking

    fun deleteBooking(bookingId: Int) : Int

    fun editBooking(booking: Booking) : Int

    fun getBooking(bookingId: Int): Booking

    fun getAllBookings(): List<Booking>

    fun findActiveBookingsByUserAndEvent(userId: Int, eventId: Int): List<Booking>

    fun findAllByStatus(status: BookingStatus): List<Booking>
}