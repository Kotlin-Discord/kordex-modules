package com.kotlindiscord.kordex

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.Paginator
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.utils.respond
import com.kotlindiscord.kordex.arguments.GenericArguments
import com.kotlindiscord.kordex.utils.*
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
            PlasmaNamespace,
            YarnNamespace,
        )
    }

    private val namespacePrefixes = mapOf(
        "y" to Namespaces["yarn"],
        "mcp" to Namespaces["mcp"],
        "mm" to Namespaces["mojang"],
        "mo" to Namespaces["mojang"],
        "p" to Namespaces["plasma"],
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
                val args = parse(::GenericArguments)

                message.respond(
                    "Namespace: `${args.ns.id}` with ${args.ns.getAllVersions().size} versions"
                )

                args.ns.getAllSortedVersions().map { println(it) }

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
                val nsProvider = args.ns.getProvider(provider.version!!)
                val pages = mutableListOf<String>()

                val hasClass = query.contains("/")
                val hasWildcard =
                    (hasClass && query.substring(0, query.lastIndexOf("/")).onlyClass() == "*") ||
                            query.onlyClass() == "*"

                message.channel.withTyping {
                    val result = MappingsQuery.queryMethods(
                        QueryContext(
                            provider = provider,
                            searchKey = query
                        )
                    ).map { it.map { inner -> inner.value }.toList() }

                    result.value.chunked(10).forEach {
                        var page = ""

                        it.forEach { (parent, method) ->
                            page += method.obfName.buildString(" => ") +
                                    method.intermediaryName +
                                    method.mappedName.mapIfNotNullOrNotEquals(method.intermediaryName) { name -> "=> `$name`" } +
                                    "\n"
                        }

                        pages.add(page)
                    }
                }

                val paginator = Paginator(
                    bot, message.channel, "Mappings", pages, message.author, keepEmbed = true
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
