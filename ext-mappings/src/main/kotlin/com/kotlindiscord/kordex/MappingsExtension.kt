package com.kotlindiscord.kordex

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.Paginator
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.utils.respond
import com.kotlindiscord.kordex.arguments.GenericArguments
import com.kotlindiscord.kordex.arguments.MCPArguments
import com.kotlindiscord.kordex.arguments.MojangArguments
import com.kotlindiscord.kordex.arguments.YarnArguments
import com.kotlindiscord.kordex.utils.classesToPages
import com.kotlindiscord.kordex.utils.fieldsToPages
import com.kotlindiscord.kordex.utils.linkie.*
import com.kotlindiscord.kordex.utils.methodsToPages
import dev.kord.core.behavior.channel.withTyping
import me.shedaniel.linkie.MappingsContainer
import me.shedaniel.linkie.MappingsProvider
import me.shedaniel.linkie.Namespace
import me.shedaniel.linkie.Namespaces
import me.shedaniel.linkie.namespaces.*

private const val VERSION_CHUNK_SIZE = 10

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

    override suspend fun setup() {
        // region: Generic mappings lookups

        // Class
        command {
            name = "class"

            description = "Look up mappings info for a class, given a specific mappings namespace.\n\n" +

                    "**Namespaces:** `mcp`, `mojang`, `yarn`\n" +
                    "**Yarn channels:** " + YarnChannels.values().joinToString(", ") { "`${it.str}`" } +
                    "\n\n" +

                    "Instead of specifying a mappings namespace here, you can use the following commands to query " +
                    "each namespace directly: `mcpc`, `mmc` or `yc`.\n\n" +

                    "For more information or a list of versions for each namespace, you can use the following " +
                    "commands: `mcp`, `mojang` or `yarn`."

            aliases = arrayOf("c")
            signature(::GenericArguments)

            action {
                val args: GenericArguments

                message.channel.withTyping {
                    args = parse(::GenericArguments)
                }

                queryClasses(args.ns, args.query, args.version, args.yarnChannel)
            }
        }

        // Field
        command {
            name = "field"

            description = "Look up mappings info for a field, given a specific mappings namespace.\n\n" +

                    "**Namespaces:** `mcp`, `mojang`, `yarn`\n" +
                    "**Yarn channels:** " + YarnChannels.values().joinToString(", ") { "`${it.str}`" } +
                    "\n\n" +

                    "Instead of specifying a mappings namespace here, you can use the following commands to query " +
                    "each namespace directly: `mcpf`, `mmf` or `yf`.\n\n" +

                    "For more information or a list of versions for each namespace, you can use the following " +
                    "commands: `mcp`, `mojang` or `yarn`."

            aliases = arrayOf("f")
            signature(::GenericArguments)

            action {
                val args: GenericArguments

                message.channel.withTyping {
                    args = parse(::GenericArguments)
                }

                queryFields(args.ns, args.query, args.version, args.yarnChannel)
            }
        }

        // Method
        command {
            name = "method"

            description = "Look up mappings info for a method, given a specific mappings namespace.\n\n" +

                    "**Namespaces:** `mcp`, `mojang`, `yarn`\n" +
                    "**Yarn channels:** " + YarnChannels.values().joinToString(", ") { "`${it.str}`" } +
                    "\n\n" +

                    "Instead of specifying a mappings namespace here, you can use the following commands to query " +
                    "each namespace directly: `mcpm`, `mmm` or `ym`.\n\n" +

                    "For more information or a list of versions for each namespace, you can use the following " +
                    "commands: `mcp`, `mojang` or `yarn`."

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

                queryMethods(args.ns, args.query, args.version, args.yarnChannel)
            }
        }

        // endregion

        // region: MCP mappings lookups

        // Class
        command {
            name = "mcpc"

            description = "Look up MCP mappings info for a class.\n\n" +

                    "For more information or a list of versions for MCP mappings, you can use the `mcp` command."

            signature(::MCPArguments)

            action {
                val args: MCPArguments

                message.channel.withTyping {
                    args = parse(::MCPArguments)
                }

                queryClasses(MCPNamespace, args.query, args.version)
            }
        }

        // Field
        command {
            name = "mcpf"

            description = "Look up MCP mappings info for a field.\n\n" +

                    "For more information or a list of versions for MCP mappings, you can use the `mcp` command."

            signature(::MCPArguments)

            action {
                val args: MCPArguments

                message.channel.withTyping {
                    args = parse(::MCPArguments)
                }

                queryFields(MCPNamespace, args.query, args.version)
            }
        }

        // Method
        command {
            name = "mcpm"

            description = "Look up MCP mappings info for a method.\n\n" +

                    "For more information or a list of versions for MCP mappings, you can use the `mcp` command."

            signature(::MCPArguments)

            action {
                val args: MCPArguments

                message.channel.withTyping {
                    args = parse(::MCPArguments)
                }

                queryMethods(MCPNamespace, args.query, args.version)
            }
        }

        // endregion

        // region: Mojang mappings lookups

        // Class
        command {
            name = "mmc"
            aliases = arrayOf("mojc", "mojmapc")

            description = "Look up Mojang mappings info for a class.\n\n" +

                    "For more information or a list of versions for Mojang mappings, you can use the `mojang` command."

            signature(::MojangArguments)

            action {
                val args: MojangArguments

                message.channel.withTyping {
                    args = parse(::MojangArguments)
                }

                queryClasses(MojangNamespace, args.query, args.version)
            }
        }

        // Field
        command {
            name = "mmf"
            aliases = arrayOf("mojf", "mojmapf")

            description = "Look up Mojang mappings info for a field.\n\n" +

                    "For more information or a list of versions for Mojang mappings, you can use the `mojang` command."

            signature(::MojangArguments)

            action {
                val args: MojangArguments

                message.channel.withTyping {
                    args = parse(::MojangArguments)
                }

                queryFields(MojangNamespace, args.query, args.version)
            }
        }

        // Method
        command {
            name = "mmm"
            aliases = arrayOf("mojm", "mojmapm")

            description = "Look up Mojang mappings info for a method.\n\n" +

                    "For more information or a list of versions for Mojang mappings, you can use the `mojang` command."

            signature(::MojangArguments)

            action {
                val args: MojangArguments

                message.channel.withTyping {
                    args = parse(::MojangArguments)
                }

                queryMethods(MojangNamespace, args.query, args.version)
            }
        }

        // endregion

        // region: Yarn mappings lookups

        // Class
        command {
            name = "yc"
            aliases = arrayOf("yarnc")

            description = "Look up Yarn mappings info for a class.\n\n" +

                    "**Yarn channels:** " + YarnChannels.values().joinToString(", ") { "`${it.str}`" } +
                    "\n\n" +

                    "For more information or a list of versions for Mojang mappings, you can use the `yarn` command."

            signature(::YarnArguments)

            action {
                val args: YarnArguments

                message.channel.withTyping {
                    args = parse(::YarnArguments)
                }

                queryClasses(YarnNamespace, args.query, args.version, args.yarnChannel)
            }
        }

        // Field
        command {
            name = "yf"
            aliases = arrayOf("yarnf")

            description = "Look up Yarn mappings info for a field.\n\n" +

                    "**Yarn channels:** " + YarnChannels.values().joinToString(", ") { "`${it.str}`" } +
                    "\n\n" +

                    "For more information or a list of versions for Mojang mappings, you can use the `yarn` command."

            signature(::YarnArguments)

            action {
                val args: YarnArguments

                message.channel.withTyping {
                    args = parse(::YarnArguments)
                }

                queryFields(YarnNamespace, args.query, args.version, args.yarnChannel)
            }
        }

        // Method
        command {
            name = "ym"
            aliases = arrayOf("yarnm")

            description = "Look up Yarn mappings info for a method.\n\n" +

                    "**Yarn channels:** " + YarnChannels.values().joinToString(", ") { "`${it.str}`" } +
                    "\n\n" +

                    "For more information or a list of versions for Mojang mappings, you can use the `yarn` command."

            signature(::YarnArguments)

            action {
                val args: YarnArguments

                message.channel.withTyping {
                    args = parse(::YarnArguments)
                }

                queryMethods(YarnNamespace, args.query, args.version, args.yarnChannel)
            }
        }

        // endregion

        // region: Mappings info commands

        // MCP
        command {
            name = "mcp"

            description = "Get information and a list of supported versions for MCP mappings."

            action {
                val defaultVersion = MCPNamespace.getDefaultVersion()
                val allVersions = MCPNamespace.getAllSortedVersions()

                val pages = allVersions.chunked(VERSION_CHUNK_SIZE).map {
                    it.joinToString("\n") { version ->
                        if (version == defaultVersion) {
                            "**» $version** (Default)"
                        } else {
                            "**»** $version"
                        }
                    }
                }.toMutableList()

                pages.add(
                    0,
                    "MCP mappings are available for queries across **${allVersions.size}** versions.\n\n" +

                            "**Default version:** $defaultVersion\n" +

                            "For a full list of supported MCP versions, please view the rest of the pages."
                )

                val paginator = Paginator(
                    bot,
                    message.channel,
                    "Mappings info: MCP",
                    pages,
                    message.author,
                    keepEmbed = true
                )

                paginator.send()
            }
        }

        // Mojang
        command {
            name = "mojang"
            aliases = arrayOf("mojmap")

            description = "Get information and a list of supported versions for Mojang mappings."

            action {
                val defaultVersion = MojangNamespace.getDefaultVersion()
                val allVersions = MojangNamespace.getAllSortedVersions()

                val pages = allVersions.chunked(VERSION_CHUNK_SIZE).map {
                    it.joinToString("\n") { version ->
                        if (version == defaultVersion) {
                            "**» $version** (Default)"
                        } else {
                            "**»** $version"
                        }
                    }
                }.toMutableList()

                pages.add(
                    0,
                    "Mojang mappings are available for queries across **${allVersions.size}** versions.\n\n" +

                            "**Default version:** $defaultVersion\n" +

                            "For a full list of supported Mojang versions, please view the rest of the pages."
                )

                val paginator = Paginator(
                    bot,
                    message.channel,
                    "Mappings info: Mojang",
                    pages,
                    message.author,
                    keepEmbed = true
                )

                paginator.send()
            }
        }

        // Yarn
        command {
            name = "yarn"

            description = "Get information and a list of supported versions for Yarn mappings."

            action {
                val defaultVersion = YarnNamespace.getDefaultVersion()
                val defaultLegacyVersion = YarnNamespace.getDefaultVersion { YarnChannels.LEGACY.str }
                val defaultPatchworkVersion = YarnNamespace.getDefaultVersion { YarnChannels.PATCHWORK.str }
                val allVersions = YarnNamespace.getAllSortedVersions()

                val pages = allVersions.chunked(VERSION_CHUNK_SIZE).map {
                    it.joinToString("\n") { version ->
                        when (version) {
                            defaultVersion -> "**» $version** (Default)"
                            defaultLegacyVersion -> "**» $version** (Default: Legacy)"
                            defaultPatchworkVersion -> "**» $version** (Default: Patchwork)"

                            else -> "**»** $version"
                        }
                    }
                }.toMutableList()

                pages.add(
                    0,
                    "Yarn mappings are available for queries across **${allVersions.size}** versions.\n\n" +

                            "**Default version:** $defaultVersion\n" +
                            "**Default Legacy version:** $defaultLegacyVersion\n" +
                            "**Default Patchwork version:** $defaultPatchworkVersion\n\n" +

                            "For a full list of supported Yarn versions, please view the rest of the pages."
                )

                val paginator = Paginator(
                    bot,
                    message.channel,
                    "Mappings info: Yarn",
                    pages,
                    message.author,
                    keepEmbed = true
                )

                paginator.send()
            }
        }

        // endregion
    }

    private suspend fun CommandContext.queryClasses(
        namespace: Namespace,
        givenQuery: String,
        version: MappingsContainer?,
        yarnChannel: YarnChannels? = null
    ) {
        if (namespace != YarnNamespace && yarnChannel != null) {
            message.respond("You may only specify a Yarn channel when looking up Yarn mappings.")
            return
        }

        val provider = if (version == null) {
            MappingsProvider.empty(namespace)
        } else {
            namespace.getProvider(version.version)
        }

        provider.injectDefaultVersion(
            namespace.getDefaultProvider {
                yarnChannel?.str ?: namespace.getDefaultMappingChannel()
            }
        )

        val query = givenQuery.replace(".", "/")
        var pages: List<String>

        message.channel.withTyping {
            @Suppress("TooGenericExceptionCaught")
            val result = try {
                MappingsQuery.queryClasses(
                    QueryContext(
                        provider = provider,
                        searchKey = query
                    )
                )
            } catch (e: NullPointerException) {
                message.respond(e.localizedMessage)
                return@queryClasses
            }

            pages = classesToPages(namespace, result)
        }

        val meta = provider.get()

        val paginator = Paginator(
            bot,
            message.channel,
            "List of ${meta.name} classes: ${meta.version}",
            pages,
            message.author,
            keepEmbed = true
        )

        paginator.send()
    }

    private suspend fun CommandContext.queryFields(
        namespace: Namespace,
        givenQuery: String,
        version: MappingsContainer?,
        yarnChannel: YarnChannels? = null
    ) {
        if (namespace != YarnNamespace && yarnChannel != null) {
            message.respond("You may only specify a Yarn channel when looking up Yarn mappings.")
            return
        }

        val provider = if (version == null) {
            MappingsProvider.empty(namespace)
        } else {
            namespace.getProvider(version.version)
        }

        provider.injectDefaultVersion(
            namespace.getDefaultProvider {
                yarnChannel?.str ?: namespace.getDefaultMappingChannel()
            }
        )

        val query = givenQuery.replace(".", "/")
        var pages: List<String>

        message.channel.withTyping {
            @Suppress("TooGenericExceptionCaught")
            val result = try {
                MappingsQuery.queryFields(
                    QueryContext(
                        provider = provider,
                        searchKey = query
                    )
                )
            } catch (e: NullPointerException) {
                message.respond(e.localizedMessage)
                return@queryFields
            }

            pages = fieldsToPages(namespace, provider.get(), result)
        }

        val meta = provider.get()

        val paginator = Paginator(
            bot,
            message.channel,
            "List of ${meta.name} fields: ${meta.version}",
            pages,
            message.author,
            keepEmbed = true
        )

        paginator.send()
    }

    private suspend fun CommandContext.queryMethods(
        namespace: Namespace,
        givenQuery: String,
        version: MappingsContainer?,
        yarnChannel: YarnChannels? = null
    ) {
        if (namespace != YarnNamespace && yarnChannel != null) {
            message.respond("You may only specify a Yarn channel when looking up Yarn mappings.")
            return
        }

        val provider = if (version == null) {
            MappingsProvider.empty(namespace)
        } else {
            namespace.getProvider(version.version)
        }

        provider.injectDefaultVersion(
            namespace.getDefaultProvider {
                yarnChannel?.str ?: namespace.getDefaultMappingChannel()
            }
        )

        val query = givenQuery.replace(".", "/")
        var pages: List<String>

        message.channel.withTyping {
            @Suppress("TooGenericExceptionCaught")
            val result = try {
                MappingsQuery.queryMethods(
                    QueryContext(
                        provider = provider,
                        searchKey = query
                    )
                )
            } catch (e: NullPointerException) {
                message.respond(e.localizedMessage)
                return@queryMethods
            }

            pages = methodsToPages(namespace, provider.get(), result)
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
