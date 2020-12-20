package com.kotlindiscord.kordex.arguments

import com.kotlindiscord.kord.extensions.commands.converters.string
import com.kotlindiscord.kord.extensions.commands.parser.Arguments
import com.kotlindiscord.kordex.converters.optionalMappingsVersion
import me.shedaniel.linkie.namespaces.MojangNamespace

/** Arguments for Mojang mappings lookup commands. **/
@Suppress("UndocumentedPublicProperty")
class MojangArguments : Arguments() {
    val query by string("query")
    val version by optionalMappingsVersion("version", true, MojangNamespace)
}
