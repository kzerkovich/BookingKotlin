package data.mappers

import data.db.entities.EventDbModel
import domain.entities.Event
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.InsertStatement

object EventMapper {
    fun fromDbModelToEntity(row: ResultRow): Event = Event(
        id = row[EventDbModel.id].value,
        name = row[EventDbModel.name],
        description = row[EventDbModel.description],
        date = Converter.convertTimestampToDate(row[EventDbModel.date]),
        location = row[EventDbModel.location],
        category = row[EventDbModel.category],
        totalTickets = row[EventDbModel.totalTickets],
        availableTickets = row[EventDbModel.availableTickets],
        price = Converter.convertDecimalToDouble(row[EventDbModel.price]),
        isCancelled = row[EventDbModel.isCancelled]
    )

    fun fromEntityToDbModel(event: Event): EventDbModel.(InsertStatement<Number>) -> Unit = {
        /*it[name] = event.name
        it[description] = event.description
        it[date] = Converter.convertDateToTimestamp(event.date)
        it[location] = event.location
        it[category] = event.category
        it[totalTickets] = event.totalTickets
        it[availableTickets] = event.availableTickets
        it[price] = Converter.convertDoubleToDecimal(event.price)
        it[isCancelled] = event.isCancelled
         */
    }
}