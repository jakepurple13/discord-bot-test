package programmersbox.com.plugins.database

import programmersbox.com.plugins.CheckTypes
import programmersbox.com.plugins.ExtensionJsonObject
import programmersbox.com.plugins.Network
import programmersbox.com.plugins.extensions.Feature

class DatabaseRepository(
    private val service: OtakuService,
    private val network: Network
) {
    suspend fun loadFromDb() = service.readAll()

    suspend fun loadFeaturesFromDb(feature: Feature, includeAll: Boolean = true) =
        if (feature == Feature.All && includeAll) {
            service.readAll()
        } else {
            service.selectFeatures(feature.name.lowercase())
        }

    suspend fun loadSources(): Map<CheckTypes, List<ExtensionJsonObject>> = runCatching {
        val sources = network.sources()
        val new = mutableListOf<ExtensionJsonObject>()
        val update = mutableListOf<ExtensionJsonObject>()

        sources.forEach {
            val dbModel = service.read(it.name)
            if (dbModel != null) {
                if (checkForUpdate(dbModel.version, it.version)) {
                    service.update(it.name, it)
                    update.add(it)
                }
            } else {
                service.create(it)
                new.add(it)
            }
        }

        mapOf(
            CheckTypes.New to new,
            CheckTypes.Update to update
        )
    }
        .onFailure { it.printStackTrace() }
        .getOrDefault(emptyMap())
}

private fun checkForUpdate(oldVersion: String, newVersion: String): Boolean = try {
    val items = oldVersion.split(".").zip(newVersion.split("."))
    val major = items[0]
    val minor = items[1]
    val patch = items[2]
    /*
     new major > old major
     new major == old major && new minor > old minor
     new major == old major && new minor == old minor && new patch > old patch
     else false
     */
    when {
        major.second.toInt() > major.first.toInt() -> true
        major.second.toInt() == major.first.toInt() && minor.second.toInt() > minor.first.toInt() -> true
        major.second.toInt() == major.first.toInt()
                && minor.second.toInt() == minor.first.toInt()
                && patch.second.toInt() > patch.first.toInt() -> true

        else -> false
    }
} catch (e: Exception) {
    false
}