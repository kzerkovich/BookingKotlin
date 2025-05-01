package domain.entities

import domain.BookingStatus
import domain.Serializer
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class Booking(
    var id: Int,
    val eventId: Int,
    val userId: Int,
    val ticketsCount: Int,
    @Serializable(with = Serializer::class)
    val bookingDate: Date,
    @Serializable(with = Serializer::class)
    val expirationDate: Date,
    val status: BookingStatus = BookingStatus.CANCELLED
)
