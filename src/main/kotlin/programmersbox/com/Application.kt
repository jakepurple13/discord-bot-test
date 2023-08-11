package programmersbox.com

import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database
import programmersbox.com.plugins.*
import programmersbox.com.plugins.database.DatabaseRepository
import programmersbox.com.plugins.database.DatabaseSetup
import programmersbox.com.plugins.database.OtakuService
import programmersbox.com.plugins.database.configureDatabases

suspend fun main(args: Array<String>) {
    val token = args.first()
    val channelId = args[1]

    val database = Database.connect(
        url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
        user = "root",
        driver = "org.h2.Driver",
        password = ""
    )

    DatabaseSetup(database)

    val otakuService = OtakuService()

    val databaseRepository = DatabaseRepository(
        service = otakuService,
        network = Network()
    )

    val settingsDb = SettingsDb()

    DiscordBot(
        token = token,
        channelId = channelId,
        otakuBot = OtakuBot(
            settingsDb = settingsDb,
            databaseRepository = databaseRepository
        )
    ) { databaseRepository.loadSources() }

    /*while (true) {
        val f = readln()
        println(f)
        c?.createMessage(f)
        if (f == "stop") break
    }*/
    //embeddedServer(CIO, port = 8080, host = "0.0.0.0", module = Application::module).start(wait = true)
}

fun Application.module() {
    configureSerialization()
    configureDatabases()
    configureRouting()
}
