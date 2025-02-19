package domain.entities

import domain.BookingStatus
import java.util.Date

data class Booking(
    var id: Int = UNDEFINED_ID,
    val eventID: Int,
    val userID: Int,
    val bookingDate: Date,
    val status: BookingStatus = BookingStatus.CANCELLED
) {
    companion object {
        const val UNDEFINED_ID = -1
    }
}
