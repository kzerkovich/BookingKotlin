package data.repositoryImpl

import data.db.entities.BookingDbModel
import data.mappers.BookingMapper
import data.mappers.Converter
import data.mappers.enums.BookingStatusMapper
import domain.BookingStatus
import domain.entities.Booking
import domain.repository.BookingRepository
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greater
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*
import kotlin.NoSuchElementException

class BookingRepositoryImpl : BookingRepository {
    override fun addBooking(booking: Booking): Booking = transaction {
        val id = BookingDbModel.insertAndGetId {
            it[eventId] = booking.eventId
            it[userId] = booking.userId
            it[ticketsCount] = booking.ticketsCount // Добавлено
            it[bookingDate] = Converter.convertDateToTimestamp(booking.bookingDate)
            it[expirationDate] = Converter.convertDateToTimestamp(booking.expirationDate) // Добавлено
            it[status] = BookingStatusMapper.fromEnumToDbModel(booking.status)
        }.value
        booking.copy(id = id)
    }

    override fun editBooking(booking: Booking) = transaction {
        BookingDbModel.update({ BookingDbModel.id eq booking.id }) {
            it[eventId] = booking.eventId
            it[userId] = booking.userId
            it[ticketsCount] = booking.ticketsCount // Добавлено
            it[bookingDate] = Converter.convertDateToTimestamp(booking.bookingDate)
            it[expirationDate] = Converter.convertDateToTimestamp(booking.expirationDate) // Добавлено
            it[status] = BookingStatusMapper.fromEnumToDbModel(booking.status)
        }
    }

    override fun deleteBooking(bookingId: Int) = transaction {
        BookingDbModel.deleteWhere { BookingDbModel.id eq bookingId }
    }


    override fun getBooking(bookingId: Int): Booking = transaction {
        BookingDbModel.selectAll().where { BookingDbModel.id eq bookingId }
            .map { row ->
                    BookingMapper.fromDbModelToEntity(row)
            }.firstOrNull() ?: throw NoSuchElementException("Booking $bookingId not found")
    }

    override fun getAllBookings(): List<Booking> = transaction {
        BookingDbModel.selectAll().map { row ->
            BookingMapper.fromDbModelToEntity(row)
        }
    }

    override fun findActiveBookingsByUserAndEvent(userId: Int, eventId: Int): List<Booking> = transaction {
        val currentTime = Converter.convertDateToTimestamp(Date())
        BookingDbModel
            .selectAll().where {
                (BookingDbModel.userId eq userId) and
                        (BookingDbModel.eventId eq eventId) and
                        (BookingDbModel.status eq BookingStatusMapper.fromEnumToDbModel(BookingStatus.PENDING)) and
                        (BookingDbModel.expirationDate greater currentTime)
            }
            .map { BookingMapper.fromDbModelToEntity(it) }
    }

    override fun findAllByStatus(status: BookingStatus): List<Booking> = transaction {
        BookingDbModel
            .selectAll().where { BookingDbModel.status eq BookingStatusMapper.fromEnumToDbModel(status) }
            .map { BookingMapper.fromDbModelToEntity(it) }
    }
}