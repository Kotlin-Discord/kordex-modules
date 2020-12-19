package com.kotlindiscord.kordex.arguments

import com.kotlindiscord.kord.extensions.commands.converters.string
import com.kotlindiscord.kord.extensions.commands.parser.Arguments
import com.kotlindiscord.kordex.converters.namespace
import com.kotlindiscord.kordex.converters.optionalMappingsVersion

class GenericArguments : Arguments() {
    val ns by namespace("namespace")
    val query by string("query")
    val version by optionalMappingsVersion("version", true) { ns }
}
