package domain.entities

import domain.Serializer
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class Event(
    var id: Int,
    var name: String,
    var description: String,
    @Serializable(with = Serializer::class)
    var date: Date,
    var location: String,
    var category: String,
    var totalTickets: Int,
    var availableTickets: Int,
    var price: Double,
    var isCancelled: Boolean = false
)
