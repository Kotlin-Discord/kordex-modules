package com.kotlindiscord.kordex.ext.mappings

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.Paginator
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.utils.respond
import com.kotlindiscord.kordex.ext.mappings.arguments.MCPArguments
import com.kotlindiscord.kordex.ext.mappings.arguments.MojangArguments
import com.kotlindiscord.kordex.ext.mappings.arguments.YarnArguments
import com.kotlindiscord.kordex.ext.mappings.configuration.MappingsConfigAdapter
import com.kotlindiscord.kordex.ext.mappings.configuration.TomlMappingsConfig
import com.kotlindiscord.kordex.ext.mappings.enums.Channels
import com.kotlindiscord.kordex.ext.mappings.enums.YarnChannels
import com.kotlindiscord.kordex.ext.mappings.exceptions.UnsupportedNamespaceException
import com.kotlindiscord.kordex.ext.mappings.utils.classesToPages
import com.kotlindiscord.kordex.ext.mappings.utils.fieldsToPages
import com.kotlindiscord.kordex.ext.mappings.utils.linkie.*
import com.kotlindiscord.kordex.ext.mappings.utils.methodsToPages
import dev.kord.core.behavior.channel.withTyping
import dev.kord.core.event.message.MessageCreateEvent
import me.shedaniel.linkie.MappingsContainer
import me.shedaniel.linkie.MappingsProvider
import me.shedaniel.linkie.Namespace
import me.shedaniel.linkie.Namespaces
import me.shedaniel.linkie.namespaces.*
import mu.KotlinLogging

private const val VERSION_CHUNK_SIZE = 10

/**
 * Extension providing Minecraft mappings lookups on Discord.
 */
class MappingsExtension(bot: ExtensibleBot) : Extension(bot) {
    companion object {
        /** Mappings configuration object. **/
        private var config: MappingsConfigAdapter = TomlMappingsConfig()

        /** Checks to apply to each command. **/
        private var checks: MutableList<suspend (String) -> (suspend (MessageCreateEvent) -> Boolean)> = mutableListOf()

        /**
         * Call this before your bot starts to change the configuration adapter used by this extension.
         */
        fun configure(configObject: MappingsConfigAdapter) {
            config = configObject
        }

        /**
         * Call this before your bot starts to add a check that will be run against every command.
         *
         * The function/lambda passed should take a [String] - the name of the command being checked - and return
         * a function taking a [MessageCreateEvent] and returning a [Boolean]. The returned function should return
         * `true` if the command should execute, and `false` if it shouldn't, using the command name and event as
         * context.
         *
         * Note that all custom checks will be run **BEFORE** checks that are specified in the configuration.
         */
        fun addCheck(check: suspend (String) -> (suspend (MessageCreateEvent) -> Boolean)) =
            checks.add(check)
    }

    private val logger = KotlinLogging.logger { }

    override val name: String = "mappings"

    override suspend fun setup() {
        val namespaces = mutableListOf<Namespace>()
        val enabledNamespaces = config.getEnabledNamespaces()

        enabledNamespaces.forEach {
            when (it) {
                "mcp" -> namespaces.add(MCPNamespace)
                "mojang" -> namespaces.add(MojangNamespace)
                "yarn" -> namespaces.add(YarnNamespace)

                else -> throw UnsupportedNamespaceException(it)
            }
        }

        if (namespaces.isEmpty()) {
            logger.warn { "No namespaces have been enabled, not registering commands." }
            return
        }

        Namespaces.init(*namespaces.toTypedArray())

        val mcpEnabled = enabledNamespaces.contains("mcp")
        val mojangEnabled = enabledNamespaces.contains("mojang")
        val yarnEnabled = enabledNamespaces.contains("yarn")

        val categoryCheck = allowedCategory(config.getAllowedCategories(), config.getBannedCategories())
        val channelCheck = allowedGuild(config.getAllowedChannels(), config.getBannedChannels())
        val guildCheck = allowedGuild(config.getAllowedGuilds(), config.getBannedGuilds())

        // region: MCP mappings lookups

        if (mcpEnabled) {
            // Class
            command {
                name = "mcpc"

                description = "Look up MCP mappings info for a class.\n\n" +

                        "For more information or a list of versions for MCP mappings, you can use the `mcp` command."

                check(customChecks(name))
                check(categoryCheck, channelCheck, guildCheck)  // Default checks
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

                check(customChecks(name))
                check(categoryCheck, channelCheck, guildCheck)  // Default checks
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

                check(customChecks(name))
                check(categoryCheck, channelCheck, guildCheck)  // Default checks
                signature(::MCPArguments)

                action {
                    val args: MCPArguments

                    message.channel.withTyping {
                        args = parse(::MCPArguments)
                    }

                    queryMethods(MCPNamespace, args.query, args.version)
                }
            }
        }

        // endregion

        // region: Mojang mappings lookups

        if (mojangEnabled) {
            // Class
            command {
                name = "mmc"
                aliases = arrayOf("mojc", "mojmapc")

                description = "Look up Mojang mappings info for a class.\n\n" +

                        "**Channels:** " + Channels.values().joinToString(", ") { "`${it.str}`" } +
                        "\n\n" +

                        "For more information or a list of versions for Mojang mappings, you can use the `mojang` " +
                        "command."

                check(customChecks(name))
                check(categoryCheck, channelCheck, guildCheck)  // Default checks
                signature(::MojangArguments)

                action {
                    val args: MojangArguments

                    message.channel.withTyping {
                        args = parse(::MojangArguments)
                    }

                    queryClasses(MojangNamespace, args.query, args.version, args.channel?.str)
                }
            }

            // Field
            command {
                name = "mmf"
                aliases = arrayOf("mojf", "mojmapf")

                description = "Look up Mojang mappings info for a field.\n\n" +

                        "**Channels:** " + Channels.values().joinToString(", ") { "`${it.str}`" } +
                        "\n\n" +

                        "For more information or a list of versions for Mojang mappings, you can use the `mojang` " +
                        "command."

                check(customChecks(name))
                check(categoryCheck, channelCheck, guildCheck)  // Default checks
                signature(::MojangArguments)

                action {
                    val args: MojangArguments

                    message.channel.withTyping {
                        args = parse(::MojangArguments)
                    }

                    queryFields(MojangNamespace, args.query, args.version, args.channel?.str)
                }
            }

            // Method
            command {
                name = "mmm"
                aliases = arrayOf("mojm", "mojmapm")

                description = "Look up Mojang mappings info for a method.\n\n" +

                        "**Channels:** " + Channels.values().joinToString(", ") { "`${it.str}`" } +
                        "\n\n" +

                        "For more information or a list of versions for Mojang mappings, you can use the `mojang` " +
                        "command."

                check(customChecks(name))
                check(categoryCheck, channelCheck, guildCheck)  // Default checks
                signature(::MojangArguments)

                action {
                    val args: MojangArguments

                    message.channel.withTyping {
                        args = parse(::MojangArguments)
                    }

                    queryMethods(MojangNamespace, args.query, args.version, args.channel?.str)
                }
            }
        }

        // endregion

        // region: Yarn mappings lookups

        if (yarnEnabled) {
            // Class
            command {
                name = "yc"
                aliases = arrayOf("yarnc")

                description = "Look up Yarn mappings info for a class.\n\n" +

                        "**Channels:** " + YarnChannels.values().joinToString(", ") { "`${it.str}`" } +
                        "\n\n" +

                        "For more information or a list of versions for Yarn mappings, you can use the `yarn` " +
                        "command."

                check(customChecks(name))
                check(categoryCheck, channelCheck, guildCheck)  // Default checks
                signature(::YarnArguments)

                action {
                    val args: YarnArguments

                    message.channel.withTyping {
                        args = parse(::YarnArguments)
                    }

                    queryClasses(YarnNamespace, args.query, args.version, args.channel?.str)
                }
            }

            // Field
            command {
                name = "yf"
                aliases = arrayOf("yarnf")

                description = "Look up Yarn mappings info for a field.\n\n" +

                        "**Channels:** " + YarnChannels.values().joinToString(", ") { "`${it.str}`" } +
                        "\n\n" +

                        "For more information or a list of versions for Yarn mappings, you can use the `yarn` " +
                        "command."

                check(customChecks(name))
                check(categoryCheck, channelCheck, guildCheck)  // Default checks
                signature(::YarnArguments)

                action {
                    val args: YarnArguments

                    message.channel.withTyping {
                        args = parse(::YarnArguments)
                    }

                    queryFields(YarnNamespace, args.query, args.version, args.channel?.str)
                }
            }

            // Method
            command {
                name = "ym"
                aliases = arrayOf("yarnm")

                description = "Look up Yarn mappings info for a method.\n\n" +

                        "**Channels:** " + YarnChannels.values().joinToString(", ") { "`${it.str}`" } +
                        "\n\n" +

                        "For more information or a list of versions for Yarn mappings, you can use the `yarn` " +
                        "command."

                check(customChecks(name))
                check(categoryCheck, channelCheck, guildCheck)  // Default checks
                signature(::YarnArguments)

                action {
                    val args: YarnArguments

                    message.channel.withTyping {
                        args = parse(::YarnArguments)
                    }

                    queryMethods(YarnNamespace, args.query, args.version, args.channel?.str)
                }
            }
        }

        // endregion

        // region: Mappings info commands

        if (mcpEnabled) {
            command {
                name = "mcp"

                description = "Get information and a list of supported versions for MCP mappings."

                check(customChecks(name))
                check(categoryCheck, channelCheck, guildCheck)  // Default checks

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
                                "**Commands:** `mcpc`, `mcpf`, `mcpm`\n\n" +

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
        }

        if (mojangEnabled) {
            command {
                name = "mojang"
                aliases = arrayOf("mojmap")

                description = "Get information and a list of supported versions for Mojang mappings."

                check(customChecks(name))
                check(categoryCheck, channelCheck, guildCheck)  // Default checks

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

                                "**Default version:** $defaultVersion\n\n" +

                                "**Channels:** " + Channels.values().joinToString(", ") { "`${it.str}`" } +
                                "\n" +
                                "**Commands:** `mmc`, `mmf`, `mmm`\n\n" +

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
        }

        if (yarnEnabled) {
            command {
                name = "yarn"

                description = "Get information and a list of supported versions for Yarn mappings."

                check(customChecks(name))
                check(categoryCheck, channelCheck, guildCheck)  // Default checks

                action {
                    val defaultVersion = YarnNamespace.getDefaultVersion()
                    val defaultLegacyVersion = YarnNamespace.getDefaultVersion { YarnChannels.LEGACY.str }
                    val defaultPatchworkVersion = YarnNamespace.getDefaultVersion { YarnChannels.PATCHWORK.str }
                    val defaultSnapshotVersion = YarnNamespace.getDefaultVersion { YarnChannels.SNAPSHOT.str }
                    val allVersions = YarnNamespace.getAllSortedVersions()

                    val pages = allVersions.chunked(VERSION_CHUNK_SIZE).map {
                        it.joinToString("\n") { version ->
                            when (version) {
                                defaultVersion -> "**» $version** (Default)"
                                defaultLegacyVersion -> "**» $version** (Default: Legacy)"
                                defaultPatchworkVersion -> "**» $version** (Default: Patchwork)"
                                defaultSnapshotVersion -> "**» $version** (Default: Snapshot)"

                                else -> "**»** $version"
                            }
                        }
                    }.toMutableList()

                    pages.add(
                        0,
                        "Yarn mappings are available for queries across **${allVersions.size}** versions.\n\n" +

                                "**Default version:** $defaultVersion\n" +
                                "**Default snapshot version:** $defaultSnapshotVersion\n\n" +

                                "**Default Legacy version:** $defaultLegacyVersion\n" +
                                "**Default Patchwork version:** $defaultPatchworkVersion\n\n" +

                                "**Channels:** " + YarnChannels.values().joinToString(", ") { "`${it.str}`" } +
                                "\n" +
                                "**Commands:** `yc`, `yf`, `ym`\n\n" +

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
        }

        // endregion

        logger.info { "Mappings extension set up - namespaces: " + enabledNamespaces.joinToString(", ") }
    }

    private suspend fun CommandContext.queryClasses(
        namespace: Namespace,
        givenQuery: String,
        version: MappingsContainer?,
        channel: String? = null
    ) {
        val provider = if (version == null) {
            if (channel != null) {
                namespace.getProvider(
                    namespace.getDefaultVersion { channel }
                )
            } else {
                MappingsProvider.empty(namespace)
            }
        } else {
            namespace.getProvider(version.version)
        }

        provider.injectDefaultVersion(
            namespace.getDefaultProvider {
                channel ?: namespace.getDefaultMappingChannel()
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
        channel: String? = null
    ) {
        val provider = if (version == null) {
            if (channel != null) {
                namespace.getProvider(
                    namespace.getDefaultVersion { channel }
                )
            } else {
                MappingsProvider.empty(namespace)
            }
        } else {
            namespace.getProvider(version.version)
        }

        provider.injectDefaultVersion(
            namespace.getDefaultProvider {
                channel ?: namespace.getDefaultMappingChannel()
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
        channel: String? = null
    ) {
        val provider = if (version == null) {
            if (channel != null) {
                namespace.getProvider(
                    namespace.getDefaultVersion { channel }
                )
            } else {
                MappingsProvider.empty(namespace)
            }
        } else {
            namespace.getProvider(version.version)
        }

        provider.injectDefaultVersion(
            namespace.getDefaultProvider {
                channel ?: namespace.getDefaultMappingChannel()
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

    private suspend fun customChecks(command: String): suspend (MessageCreateEvent) -> Boolean {
        val allChecks = checks.map { it.invoke(command) }

        suspend fun inner(event: MessageCreateEvent): Boolean =
            allChecks.all { it.invoke(event) }

        return ::inner
    }
}
