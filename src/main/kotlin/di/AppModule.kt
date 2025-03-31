
package di

import data.repositoryImpl.*
import org.koin.dsl.module
import domain.repository.*
import domain.usecases.*
import org.koin.core.qualifier.named

val appModule = module {
    single<BookingRepository> { BookingRepositoryImpl() }
    single<EventRepository> { EventRepositoryImpl() }
    single<UsersRepository> { UsersRepositoryImpl() }
    single<TicketRepository> { TicketRepositoryImpl() }

    single (named("bookingController")) { BookingUseCase(get<BookingRepository>(), get<EventRepository>(), get<UsersRepository>()) }
    single (named("eventController")) { EventUseCase(get<EventRepository>(), get<TicketRepository>()) }
    single (named("userController")) { UserUseCase(get<UsersRepository>()) }
    single (named("ticketController")) { TicketUseCase(get<TicketRepository>(), get<EventRepository>()) }
}