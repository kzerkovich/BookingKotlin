package domain.entities

import domain.Serializer
import domain.TicketStatus
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class Ticket(
    var id: Int,
    val eventId: Int,
    val userId: Int,
    val bookingId: Int? = null,
    var status: TicketStatus = TicketStatus.AVAILABLE,
    @Serializable(with = Serializer::class)
    var purchaseDate: Date? = null
)
