package com.kotlindiscord.kordex.converters

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.ParseException
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.SingleConverter
import me.shedaniel.linkie.MappingsContainer
import me.shedaniel.linkie.Namespace
import me.shedaniel.linkie.Namespaces

class NamespaceConverter: SingleConverter<Namespace>() {
    override val signatureTypeString: String = "mappings"

    override suspend fun parse(arg: String, context: CommandContext, bot: ExtensibleBot): Boolean {
        this.parsed = Namespaces.namespaces[arg] ?: throw ParseException("Invalid mappings namespace: $arg")

        return true
    }
}
