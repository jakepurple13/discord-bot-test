@file:Suppress("INLINE_FROM_HIGHER_PLATFORM")

package programmersbox.com.plugins.extensions

import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.editingPaginator
import programmersbox.com.plugins.DatabaseRepository
import programmersbox.com.plugins.Emerald
import java.util.*

class ShowFeaturesExtension(
    private val databaseRepository: DatabaseRepository
) : Extension() {
    override val name: String = "showfeatures"

    override suspend fun setup() {
        ephemeralSlashCommand(arguments = { FeatureArgs(true) }) {
            name = "showfeatures"
            description = "Show all the sources of a chosen feature"

            action {
                val feature = arguments.feature
                val list = databaseRepository
                    .loadFeaturesFromDb(feature)
                    .groupBy { it.feature }
                editingPaginator {
                    if (list.isNotEmpty()) {
                        list.forEach {
                            page {
                                title = it.key
                                    .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                                color = Emerald
                                it.value.forEach { field(it.name) { it.version } }
                            }
                        }
                    }
                }.send()
            }
        }
    }
}