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
        ticketsCount = row[BookingDbModel.ticketsCount],
        bookingDate = Converter.convertTimestampToDate(row[BookingDbModel.bookingDate]),
        expirationDate = Converter.convertTimestampToDate(row[BookingDbModel.expirationDate]),
        status = BookingStatusMapper.fromDbModelToEnum(row[BookingDbModel.status])
    )

    fun fromEntityToDbModel(booking: Booking): BookingDbModel.(InsertStatement<Number>) -> Unit = {
       /* it[eventId] = booking.eventId
        it[userId] = booking.userId
        it[ticketsCount] = booking.ticketsCount
        it[bookingDate] = Converter.convertDateToTimestamp(booking.bookingDate)
        it[expirationDate] = Converter.convertDateToTimestamp(booking.expirationDate)
        it[status] = BookingStatusMapper.fromEnumToDbModel(booking.status)
        */
    }
}
