package programmersbox.com.plugins.database

import dev.kord.common.entity.Snowflake
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.update
import programmersbox.com.plugins.PlayerInfo

class GamesDatabase {
    object GamesInfo : Table() {
        val snowflakeId = text("name")
        val money = double("money")

        override val primaryKey: PrimaryKey = PrimaryKey(snowflakeId)
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    suspend fun create(snowflake: Snowflake) = dbQuery {
        GamesInfo.insert {
            it[snowflakeId] = snowflake.toString()
            it[money] = 500.0
        }.let {
            PlayerInfo(
                money = it[GamesInfo.money],
                snowflake = snowflake
            )
        }
    }

    suspend fun read(snowflake: Snowflake) = dbQuery {
        GamesInfo
            .select { GamesInfo.snowflakeId eq snowflake.toString() }
            .singleOrNull()
            ?.let {
                PlayerInfo(
                    snowflake = snowflake,
                    money = it[GamesInfo.money]
                )
            }
    }

    suspend fun update(id: Snowflake, playerInfo: PlayerInfo) {
        dbQuery {
            GamesInfo.update(
                { GamesInfo.snowflakeId eq id.toString() }
            ) {
                it[money] = playerInfo.money
            }
        }
    }
}