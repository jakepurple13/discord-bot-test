@file:Suppress("INLINE_FROM_HIGHER_PLATFORM")

package programmersbox.com.plugins.extensions

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.converters.ChoiceEnum
import com.kotlindiscord.kord.extensions.commands.application.slash.converters.impl.enumChoice
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.rest.builder.message.create.embed
import programmersbox.com.plugins.DatabaseRepository
import programmersbox.com.plugins.Emerald
import java.util.*

class ShowFeaturesExtension(
    private val databaseRepository: DatabaseRepository
) : Extension() {
    override val name: String = "showfeatures"

    override suspend fun setup() {
        publicSlashCommand(::FeatureArgs) {
            name = "showfeatures"
            description = "Show all the sources of a chosen feature"

            action {
                val feature = arguments.feature
                respond {
                    val list = databaseRepository.loadFeaturesFromDb(feature.name.lowercase())
                    if (list.isNotEmpty()) {
                        embed {
                            title = feature.readableName
                                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                            color = Emerald
                            list.forEach { field(it.name) { it.version } }
                        }
                    }
                }
            }
        }
    }

    inner class FeatureArgs : Arguments() {
        val feature by enumChoice<Feature> {
            name = "feature"
            description = "Feature Type"
            typeName = "Feature"
            choices(Feature.entries.associateBy { it.name })
        }
    }

    enum class Feature(override val readableName: String) : ChoiceEnum {
        Anime("Anime"),
        Manga("Manga"),
        Novel("Novel")
    }
}