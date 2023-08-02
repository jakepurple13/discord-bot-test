package programmersbox.com.plugins

import io.realm.kotlin.MutableRealm
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.ext.asFlow
import io.realm.kotlin.migration.AutomaticSchemaMigration
import io.realm.kotlin.types.RealmObject
import kotlinx.coroutines.flow.mapNotNull

class SettingsDb(name: String = Realm.DEFAULT_FILE_NAME) {
    private val realm by lazy {
        Realm.open(
            RealmConfiguration.Builder(
                setOf(
                    Settings::class
                )
            )
                .schemaVersion(1)
                .name(name)
                .migration(AutomaticSchemaMigration { })
                //.deleteRealmIfMigrationNeeded()
                .build()
        )
    }

    private val settingsDb = realm.initDbBlocking { Settings() }

    fun getSettings() = settingsDb
        .asFlow()
        .mapNotNull { it.obj }

    suspend fun updateSettings(block: Settings.() -> Unit) {
        realm.updateInfo<Settings> { it?.let { it1 -> block(it1) } }
    }
}

private suspend inline fun <reified T : RealmObject> Realm.updateInfo(crossinline block: MutableRealm.(T?) -> Unit) {
    query(T::class).first().find()?.also { info ->
        write { block(findLatest(info)) }
    }
}

private inline fun <reified T : RealmObject> Realm.initDbBlocking(crossinline default: () -> T): T {
    val f = query(T::class).first().find()
    return f ?: writeBlocking { copyToRealm(default()) }
}