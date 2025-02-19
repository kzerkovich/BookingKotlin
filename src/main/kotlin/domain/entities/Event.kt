package domain.entities

import java.util.Date

data class Event(
    var id: Int = UNDEFINED_ID,
    var name: String,
    var date: Date,
    var location: String,
    val category: String,
    val availableTickets: Int,
    val price: Int
) {
    companion object {
        const val UNDEFINED_ID = -1
    }
}
