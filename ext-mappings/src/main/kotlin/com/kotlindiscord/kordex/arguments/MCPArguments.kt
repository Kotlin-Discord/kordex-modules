package com.kotlindiscord.kordex.arguments

import com.kotlindiscord.kord.extensions.commands.converters.string
import com.kotlindiscord.kord.extensions.commands.parser.Arguments
import com.kotlindiscord.kordex.converters.optionalMappingsVersion
import me.shedaniel.linkie.namespaces.MCPNamespace

/** Arguments for MCP mappings lookup commands. **/
@Suppress("UndocumentedPublicProperty")
class MCPArguments : Arguments() {
    val query by string("query")
    val version by optionalMappingsVersion("version", true, MCPNamespace)
}
