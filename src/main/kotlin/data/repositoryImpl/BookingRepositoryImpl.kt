package data.repositoryImpl

import domain.entities.Booking
import domain.repository.BookingRepository

class BookingRepositoryImpl(): BookingRepository {
    var bookingList = mutableListOf<Booking>()
    private var autoIncrementId = 0

    override fun addBooking(booking: Booking) {
        if (booking.id == Booking.UNDEFINED_ID)
            booking.id = autoIncrementId++
        bookingList.add(booking)
    }

    override fun deleteBooking(booking: Booking) {
        val id = bookingList.indexOf(booking)
        bookingList.removeAt(id)
    }

    override fun editBooking(booking: Booking) {
        val oldBooking = getBooking(booking.id)
        bookingList.remove(oldBooking)
        addBooking(booking)
    }

    override fun getBooking(bookingId: Int): Booking {
        return bookingList.find {
            it.id == bookingId
        } ?: throw RuntimeException("Element with id = $bookingId not found")
    }

    override fun getAllBookings(): List<Booking> {
        return bookingList
    }
}