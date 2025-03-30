package domain.entities

import java.util.*

data class Event(
    var id: Int,
    var name: String,
    var date: Date,
    var location: String,
    val category: String,
    val availableTickets: Int,
    val price: Double
)
