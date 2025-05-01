package api.dto

import domain.entities.Event
import kotlinx.serialization.Serializable
import org.valiktor.functions.*
import org.valiktor.validate
import java.util.*

@Serializable
data class CreateEventRequest(
    val name: String,
    val description: String,
    val date: Long, // Timestamp
    val location: String,
    val category: String,
    val totalTickets: Int,
    val price: Double
) {
    fun toEntity() = Event(
        id = 0,
        name = name,
        description = description,
        date = Date(date),
        location = location,
        category = category,
        totalTickets = totalTickets,
        availableTickets = totalTickets,
        price = price,
        isCancelled = false
    )

    fun validate() = validate(this) {
        validate(CreateEventRequest::name).isNotBlank()
        validate(CreateEventRequest::date).isGreaterThan(Date().time)
        validate(CreateEventRequest::totalTickets).isPositive()
        validate(CreateEventRequest::price).isPositive()
    }
}

@Serializable
data class UpdateEventRequest(
    val name: String? = null,
    val description: String? = null,
    val date: Long? = null,
    val location: String? = null,
    val category: String? = null
) {
    fun applyTo(event: Event): Event {
        name?.let { event.name = it }
        description?.let { event.description = it }
        date?.let { event.date = Date(it) }
        location?.let { event.location = it }
        category?.let { event.category = it }
        return event
    }
}

@Serializable
data class EventResponse(
    val id: Int,
    val name: String,
    val description: String,
    val date: Long,
    val location: String,
    val category: String,
    val totalTickets: Int,
    val availableTickets: Int,
    val price: Double,
    val isCancelled: Boolean
) {
    constructor(event: Event) : this(
        id = event.id,
        name = event.name,
        description = event.description,
        date = event.date.time,
        location = event.location,
        category = event.category,
        totalTickets = event.totalTickets,
        availableTickets = event.availableTickets,
        price = event.price,
        isCancelled = event.isCancelled
    )
}