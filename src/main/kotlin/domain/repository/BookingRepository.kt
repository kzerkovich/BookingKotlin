package domain.repository

import domain.entities.Booking

interface BookingRepository {
    fun addBooking(booking: Booking) : Booking

    fun deleteBooking(bookingId: Int) : Int

    fun editBooking(booking: Booking) : Int

    fun getBooking(bookingId: Int): Booking

    fun getAllBookings(): List<Booking>
}