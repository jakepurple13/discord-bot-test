package programmersbox.com.plugins.database

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

class DatabaseSetup(private val database: Database) {
    init {
        transaction(database) {
            SchemaUtils.create(
                OtakuService.OtakuSources,
                GamesDatabase.GamesInfo
            )
        }
    }
}