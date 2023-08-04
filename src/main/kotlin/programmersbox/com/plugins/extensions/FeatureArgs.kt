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
        if (!includeAll) choices.remove(Feature.All.readableName)
    }
}

enum class Feature(override val readableName: String) : ChoiceEnum {
    Anime("Anime"),
    Manga("Manga"),
    Novel("Novel"),
    All("All")
}