package com.kotlindiscord.kordex.ext.mappings.converters

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.ParseException
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.SingleConverter
import me.shedaniel.linkie.MappingsContainer
import me.shedaniel.linkie.Namespace

/**
 * Argument converter for [MappingsContainer] objects based on mappings versions.
 */
class MappingsVersionConverter(
    private val namespaceGetter: suspend () -> Namespace
) : SingleConverter<MappingsContainer>() {
    override val signatureTypeString: String = "version"
    override val showTypeInSignature: Boolean = false

    override suspend fun parse(arg: String, context: CommandContext, bot: ExtensibleBot): Boolean {
        val namespace = namespaceGetter.invoke()

        if (arg in namespace.getAllVersions()) {
            val version = namespace[arg]

            if (version == null) {
                val created = namespace.createAndAdd(arg)

                if (created != null) {
                    this.parsed = created
                } else {
                    throw ParseException("Invalid ${namespace.id} version: `$arg`")
                }
            } else {
                this.parsed = version
            }

            return true
        }

        throw ParseException("Invalid ${namespace.id} version: `$arg`")
    }
}
