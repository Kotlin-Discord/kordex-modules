package com.kotlindiscord.kordex

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.Paginator
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.utils.respond
import com.kotlindiscord.kordex.arguments.GenericArguments
import com.kotlindiscord.kordex.utils.linkie.*
import com.kotlindiscord.kordex.utils.methodsToPages
import dev.kord.core.behavior.channel.withTyping
import me.shedaniel.linkie.MappingsProvider
import me.shedaniel.linkie.Namespaces
import me.shedaniel.linkie.namespaces.*
import me.shedaniel.linkie.utils.onlyClass

/**
 * Extension providing Minecraft mappings lookups on Discord.
 */
class MappingsExtension(bot: ExtensibleBot) : Extension(bot) {
    override val name: String = "mappings"

    init {
        Namespaces.init(
            MCPNamespace,
            MojangNamespace,
            YarnNamespace,
        )
    }

    private val namespacePrefixes = mapOf(
        "y" to Namespaces["yarn"],
        "mcp" to Namespaces["mcp"],
        "mm" to Namespaces["mojang"],
        "mo" to Namespaces["mojang"]
    )

    override suspend fun setup() {
        command {
            name = "method"

            aliases = arrayOf(
                "m",  // Short command name

//                "mcpm",  // MCP
//                "mmm", "mojmapm",  // Mojang
//                "plasmam",  // Plasma
//                "ym", "yarnm",  // Yarn
            )

            signature(::GenericArguments)

            action {
                val args: GenericArguments

                message.channel.withTyping {
                    args = parse(::GenericArguments)
                }

                val provider = if (args.version == null) {
                    MappingsProvider.empty(args.ns)
                } else {
                    args.ns.getProvider(args.version!!.version)
                }

                provider.injectDefaultVersion(
                    // TODO: For Yarn command, support "legacy" and "patchwork" channels
                    args.ns.getDefaultProvider()
                )

                val query = args.query.replace(".", "/")
                var pages: List<String>

                message.channel.withTyping {
                    val result = MappingsQuery.queryMethods(
                        QueryContext(
                            provider = provider,
                            searchKey = query
                        )
                    )

                    pages = methodsToPages(args.ns, provider.get(), result)
                }

                val meta = provider.get()

                val paginator = Paginator(
                    bot,
                    message.channel,
                    "List of ${meta.name} methods: ${meta.version}",
                    pages,
                    message.author,
                    keepEmbed = true
                )

                paginator.send()
            }
        }
    }

    private fun getNamespace(name: String) =
        namespacePrefixes.mapNotNull { (key, value) ->
            if (name.startsWith(key)) {
                value
            } else {
                null
            }
        }.firstOrNull()
}
