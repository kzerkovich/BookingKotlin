package data.mappers

import data.db.entities.BookingDbModel
import data.mappers.enums.BookingStatusMapper
import domain.entities.Booking
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.InsertStatement

object BookingMapper {
    fun fromDbModelToEntity(row: ResultRow): Booking = Booking(
        id = row[BookingDbModel.id].value,
        eventId = row[BookingDbModel.eventId],
        userId = row[BookingDbModel.userId],
        bookingDate = Converter.convertTimestampToDate(row[BookingDbModel.bookingDate]),
        status = BookingStatusMapper.fromDbModelToEnum(row[BookingDbModel.status])
    )

    fun fromEntityToDbModel(booking: Booking): BookingDbModel.(InsertStatement<Number>) -> Unit = {
        TODO("The mapper from the application to the database is not working, most likely, the function signature" +
                " is incorrect")
//        it[eventId] = booking.eventId
//        it[userId] = booking.userId
//        it[bookingDate] = Converter.convertDateToTimestamp(booking.bookingDate)
//        it[status] = BookingStatusMapper.fromEnumToDbModel(booking.status)
    }
}
