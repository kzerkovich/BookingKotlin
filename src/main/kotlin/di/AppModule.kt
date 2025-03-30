
package di

import data.repositoryImpl.*
import org.koin.dsl.module
import domain.repository.*
import domain.usecases.*

val appModule = module {
    single<BookingRepository> { BookingRepositoryImpl() }
    single<EventRepository> { EventRepositoryImpl() }
    single<UsersRepository> { UsersRepositoryImpl() }
    single<TicketRepository> { TicketRepositoryImpl() }

    single { BookingUseCase(get(), get(), get()) }
    single { EventUseCase(get(), get()) }
    single { UserUseCase(get()) }
    single { TicketUseCase(get(), get()) }
}