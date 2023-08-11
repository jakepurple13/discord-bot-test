package programmersbox.com.plugins.database

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import programmersbox.com.plugins.ExtensionJsonObject

class OtakuService {
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

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    suspend fun readAll() = dbQuery {
        OtakuSources
            .selectAll()
            .map { it.toExtensionJsonObject() }
    }

    suspend fun selectFeatures(feature: String) = dbQuery {
        OtakuSources
            .select { OtakuSources.feature eq feature }
            .map { it.toExtensionJsonObject() }
    }

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
                .map { it.toExtensionJsonObject() }
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

    private fun ResultRow.toExtensionJsonObject() = ExtensionJsonObject(
        name = this[OtakuSources.name],
        pkg = this[OtakuSources.pkg],
        apk = this[OtakuSources.apk],
        lang = this[OtakuSources.lang],
        code = this[OtakuSources.code],
        version = this[OtakuSources.version],
        feature = this[OtakuSources.feature],
        sources = emptyList()
    )
}
