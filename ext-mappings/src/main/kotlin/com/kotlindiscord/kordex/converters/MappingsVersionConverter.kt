package com.kotlindiscord.kordex.converters

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.ParseException
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.SingleConverter
import me.shedaniel.linkie.MappingsContainer
import me.shedaniel.linkie.Namespace

class MappingsVersionConverter(
    private val namespaceGetter: suspend () -> Namespace
) : SingleConverter<MappingsContainer>() {
    override val signatureTypeString: String = "version"

    override suspend fun parse(arg: String, context: CommandContext, bot: ExtensibleBot): Boolean {
        val namespace = namespaceGetter.invoke()

        if (arg in namespace.getAllVersions()) {
            this.parsed = namespace[arg]!!

            return true
        }

        throw ParseException("Invalid ${namespace.id} version: `$arg`")
    }
}
