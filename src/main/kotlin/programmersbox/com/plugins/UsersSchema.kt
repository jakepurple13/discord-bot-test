package programmersbox.com.plugins

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

class OtakuService(private val database: Database) {
    object OtakuSources : Table() {
        val name = text("name")
        val pkg = text("pkg")
        val apk = text("apk")
        val lang = text("lang")
        val code = long("code")
        val version = text("version")
        val feature = text("feature")

        override val primaryKey: PrimaryKey = PrimaryKey(name)
    }

    init {
        transaction(database) {
            SchemaUtils.create(OtakuSources)
        }
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    suspend fun create(source: ExtensionJsonObject) = dbQuery {
        OtakuSources.insert {
            it[name] = source.name
            it[pkg] = source.pkg
            it[apk] = source.apk
            it[lang] = source.lang
            it[code] = source.code
            it[version] = source.version
            it[feature] = source.feature
        }[OtakuSources.name]
    }

    suspend fun read(name: String): ExtensionJsonObject? {
        return dbQuery {
            OtakuSources.select { OtakuSources.name eq name }
                .map {
                    ExtensionJsonObject(
                        name = it[OtakuSources.name],
                        pkg = it[OtakuSources.pkg],
                        apk = it[OtakuSources.apk],
                        lang = it[OtakuSources.lang],
                        code = it[OtakuSources.code],
                        version = it[OtakuSources.version],
                        feature = it[OtakuSources.feature],
                        sources = emptyList()
                    )
                }
                .singleOrNull()
        }
    }

    suspend fun update(name: String, source: ExtensionJsonObject) {
        dbQuery {
            OtakuSources.update({ OtakuSources.name eq name }) {
                it[OtakuSources.name] = source.name
                it[pkg] = source.pkg
                it[apk] = source.apk
                it[lang] = source.lang
                it[code] = source.code
                it[version] = source.version
                it[feature] = source.feature
            }
        }
    }

    suspend fun delete(name: String) {
        dbQuery {
            OtakuSources.deleteWhere { OtakuSources.name.eq(name) }
        }
    }
}
