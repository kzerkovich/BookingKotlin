import data.db.DatabaseFactory
import di.appModule
import org.koin.core.context.GlobalContext.startKoin

fun main() {
    startKoin {
        modules(appModule)
    }

    DatabaseFactory.init()
}