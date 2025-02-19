package domain.repository

import domain.entities.Booking

interface BookingRepository {
    fun addBooking(booking: Booking)

    fun deleteBooking(booking: Booking)

    fun editBooking(booking: Booking)

    fun getBooking(bookingId: Int): Booking

    fun getAllBookings(): List<Booking>
}