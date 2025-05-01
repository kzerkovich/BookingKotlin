package data.repositoryImpl

import data.db.entities.EventDbModel
import data.mappers.Converter
import data.mappers.EventMapper
import domain.entities.Event
import domain.repository.EventRepository
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.lessEq
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*
import kotlin.NoSuchElementException

class EventRepositoryImpl : EventRepository {
    override fun addEvent(event: Event): Event = transaction {
        val id = EventDbModel.insertAndGetId {
            it[name] = event.name
            it[description] = event.description
            it[date] = Converter.convertDateToTimestamp(event.date)
            it[location] = event.location
            it[category] = event.category
            it[totalTickets] = event.totalTickets
            it[availableTickets] = event.availableTickets
            it[price] = Converter.convertDoubleToDecimal(event.price)
            it[isCancelled] = event.isCancelled
        }.value
        event.copy(id = id)
    }

    override fun editEvent(event: Event) = transaction {
        EventDbModel.update({ EventDbModel.id eq event.id }) {
            it[name] = event.name
            it[description] = event.description
            it[date] = Converter.convertDateToTimestamp(event.date)
            it[location] = event.location
            it[category] = event.category
            it[totalTickets] = event.totalTickets
            it[availableTickets] = event.availableTickets
            it[price] = Converter.convertDoubleToDecimal(event.price)
            it[isCancelled] = event.isCancelled
        }
    }

    override fun deleteEvent(eventId: Int) = transaction {
        EventDbModel.deleteWhere { EventDbModel.id eq eventId }
    }

    override fun getEvent(eventId: Int): Event = transaction {
        EventDbModel.selectAll().where { EventDbModel.id eq eventId }
            .map { row ->
                EventMapper.fromDbModelToEntity(row)
            }.firstOrNull() ?: throw NoSuchElementException("Event $eventId not found")
    }

    override fun getAllEvents(): List<Event> = transaction {
        EventDbModel.selectAll().map { row ->
           EventMapper.fromDbModelToEntity(row)
        }
    }

    override fun getEventsByFilters(
        location: String?,
        category: String?,
        startDate: Date?,
        endDate: Date?
    ): List<Event> = transaction {
        EventDbModel.selectAll().where {
            val conditions = listOfNotNull(
                location?.let { EventDbModel.location eq it },
                category?.let { EventDbModel.category eq it },
                startDate?.let { EventDbModel.date greaterEq Converter.convertDateToTimestamp(it) },
                endDate?.let { EventDbModel.date lessEq Converter.convertDateToTimestamp(it) }
            )
            conditions.reduceOrNull { acc, op -> acc and op } ?: Op.TRUE
        }.map { EventMapper.fromDbModelToEntity(it) }
    }

    override fun cancelEvent(eventId: Int): Int = transaction {
        EventDbModel.update({ EventDbModel.id eq eventId }) {
            it[isCancelled] = true
        }
    }
}