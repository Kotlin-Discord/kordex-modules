package com.kotlindiscord.kordex.arguments

import com.kotlindiscord.kord.extensions.commands.converters.optionalEnum
import com.kotlindiscord.kord.extensions.commands.converters.string
import com.kotlindiscord.kord.extensions.commands.parser.Arguments
import com.kotlindiscord.kordex.YarnChannels
import com.kotlindiscord.kordex.converters.optionalMappingsVersion
import me.shedaniel.linkie.namespaces.YarnNamespace

/** Arguments for Yarn mappings lookup commands. **/
@Suppress("UndocumentedPublicProperty")
class YarnArguments : Arguments() {
    val query by string("query")
    val version by optionalMappingsVersion("version", true, YarnNamespace)
    val yarnChannel by optionalEnum<YarnChannels>("yarn-channel", "legacy/patchwork")
}
