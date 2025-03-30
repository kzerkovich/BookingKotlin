package data.repositoryImpl

import data.db.entities.EventDbModel
import data.mappers.Converter
import data.mappers.EventMapper
import domain.entities.Event
import domain.repository.EventRepository
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

class EventRepositoryImpl : EventRepository {
    override fun addEvent(event: Event): Event = transaction {
        val id = EventDbModel.insertAndGetId {
            it[name] = event.name
            it[date] = Converter.convertDateToTimestamp(event.date)
            it[location] = event.location
            it[category] = event.category
            it[availableTickets] = event.availableTickets
            it[price] = Converter.convertDoubleToDecimal(event.price)
        }.value

        event.copy(id = id)
    }

    override fun deleteEvent(eventId: Int) = transaction {
        EventDbModel.deleteWhere { EventDbModel.id eq eventId }
    }

    override fun editEvent(event: Event) = transaction {
        EventDbModel.update({ EventDbModel.id eq event.id }) {
            it[name] = event.name
            it[date] = Converter.convertDateToTimestamp(event.date)
            it[location] = event.location
            it[category] = event.category
            it[availableTickets] = event.availableTickets
            it[price] = Converter.convertDoubleToDecimal(event.price)
        }
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
}