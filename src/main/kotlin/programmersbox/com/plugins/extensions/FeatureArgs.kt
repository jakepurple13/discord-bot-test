@file:Suppress("INLINE_FROM_HIGHER_PLATFORM")

package programmersbox.com.plugins.extensions

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.converters.ChoiceEnum
import com.kotlindiscord.kord.extensions.commands.application.slash.converters.impl.enumChoice

class FeatureArgs(
    includeAll: Boolean = false
) : Arguments() {
    val feature by enumChoice<Feature> {
        name = "feature"
        description = "Feature Type"
        typeName = "Feature"
        choice(Feature.Anime.name, Feature.Anime)
        choice(Feature.Manga.name, Feature.Manga)
        choice(Feature.Novel.name, Feature.Novel)
        if (includeAll) choice(Feature.All.name, Feature.All)
    }
}

enum class Feature(override val readableName: String) : ChoiceEnum {
    Anime("Anime"),
    Manga("Manga"),
    Novel("Novel"),
    All("All")
}