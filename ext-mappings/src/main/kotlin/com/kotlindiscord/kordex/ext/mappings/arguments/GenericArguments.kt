package com.kotlindiscord.kordex.ext.mappings.arguments

import com.kotlindiscord.kord.extensions.commands.converters.optionalEnum
import com.kotlindiscord.kord.extensions.commands.converters.string
import com.kotlindiscord.kord.extensions.commands.parser.Arguments
import com.kotlindiscord.kordex.ext.mappings.YarnChannels
import com.kotlindiscord.kordex.ext.mappings.converters.namespace
import com.kotlindiscord.kordex.ext.mappings.converters.optionalMappingsVersion

/** Arguments for generic mappings lookup commands. **/
@Suppress("UndocumentedPublicProperty")
class GenericArguments : Arguments() {
    val ns by namespace("namespace")
    val query by string("query")
    val version by optionalMappingsVersion("version", true) { ns }
    val yarnChannel by optionalEnum<YarnChannels>("yarn-channel", "legacy/patchwork")
}
