package data.mappers

import kotlinx.datetime.Instant
import java.math.BigDecimal
import java.util.*

object Converter {

    fun convertTimestampToDate(row: Instant) : Date {
        return Date(row.toEpochMilliseconds())
    }

    fun convertDateToTimestamp(date: Date) : Instant {
        return Instant.fromEpochMilliseconds(date.time)
    }

    fun convertDecimalToDouble(row: BigDecimal) : Double {
        return row.toDouble()
    }

    fun convertDoubleToDecimal(price: Double) : BigDecimal {
        return price.toBigDecimal()
    }
}