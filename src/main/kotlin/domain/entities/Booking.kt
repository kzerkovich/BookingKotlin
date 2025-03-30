package domain.entities

import domain.BookingStatus
import java.util.Date

data class Booking(
    var id: Int,
    val eventId: Int,
    val userId: Int,
    val bookingDate: Date,
    val status: BookingStatus = BookingStatus.CANCELLED
)
