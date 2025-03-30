package data.mappers

import data.db.entities.EventDbModel
import domain.entities.Event
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.InsertStatement

object EventMapper {
    fun fromDbModelToEntity(row: ResultRow): Event = Event(
        id = row[EventDbModel.id].value,
        name = row[EventDbModel.name],
        date = Converter.convertTimestampToDate(row[EventDbModel.date]),
        location = row[EventDbModel.location],
        category = row[EventDbModel.category],
        availableTickets = row[EventDbModel.availableTickets],
        price = Converter.convertDecimalToDouble(row[EventDbModel.price])
    )

    fun fromEntityToDbModel(event: Event): EventDbModel.(InsertStatement<Number>) -> Unit = {
        TODO("The mapper from the application to the database is not working, most likely, the function signature" +
                " is incorrect")
//        it[name] = event.name
//        it[date] = Converter.convertDateToTimestamp(event.date)
//        it[location] = event.location
//        it[category] = event.category
//        it[availableTickets] = event.availableTickets
//        it[price] = Converter.convertDoubleToDecimal(event.price)
    }
}