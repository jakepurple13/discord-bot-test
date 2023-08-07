@file:Suppress("INLINE_FROM_HIGHER_PLATFORM")

package programmersbox.com.plugins.extensions

import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.editingPaginator
import programmersbox.com.plugins.DatabaseRepository
import programmersbox.com.plugins.Emerald
import programmersbox.com.plugins.apkUrl

class ShowFeatureUrlExtension(
    private val databaseRepository: DatabaseRepository
) : Extension() {
    override val name: String = "showsourceurl"

    override suspend fun setup() {
        ephemeralSlashCommand(::FeatureArgs) {
            name = "showsourceurl"
            description = "Get the url to download a source"

            action {
                val feature = arguments.feature
                val list = databaseRepository
                    .loadFeaturesFromDb(feature, false)
                    .chunked(4)
                editingPaginator {
                    list.forEach { l ->
                        page {
                            title = feature.readableName
                            l.forEach { field(it.name + " - " + it.version) { it.apkUrl } }
                            color = Emerald
                        }
                    }
                }.send()
            }
        }
    }
}