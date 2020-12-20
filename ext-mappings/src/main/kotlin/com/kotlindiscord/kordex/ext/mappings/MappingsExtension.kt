package com.kotlindiscord.kordex.ext.mappings

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.Paginator
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.utils.respond
import com.kotlindiscord.kordex.ext.mappings.arguments.GenericArguments
import com.kotlindiscord.kordex.ext.mappings.arguments.MCPArguments
import com.kotlindiscord.kordex.ext.mappings.arguments.MojangArguments
import com.kotlindiscord.kordex.ext.mappings.arguments.YarnArguments
import com.kotlindiscord.kordex.ext.mappings.configuration.MappingsConfigAdapter
import com.kotlindiscord.kordex.ext.mappings.configuration.TomlMappingsConfig
import com.kotlindiscord.kordex.ext.mappings.exceptions.UnsupportedNamespaceException
import com.kotlindiscord.kordex.ext.mappings.utils.classesToPages
import com.kotlindiscord.kordex.ext.mappings.utils.fieldsToPages
import com.kotlindiscord.kordex.ext.mappings.utils.linkie.*
import com.kotlindiscord.kordex.ext.mappings.utils.methodsToPages
import dev.kord.core.behavior.channel.withTyping
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
        var config: MappingsConfigAdapter = TomlMappingsConfig()

        /**
         * Call this before your bot starts to change the configuration adapter used by this extension.
         */
        fun configure(configObject: MappingsConfigAdapter) {
            config = configObject
        }
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

        val namespaceNames = getNamespaceNames()
        val classCommands = getNamespaceCommands("c")
        val fieldCommands = getNamespaceCommands("f")
        val methodCommands = getNamespaceCommands("m")

        val mcpEnabled = enabledNamespaces.contains("mcp")
        val mojangEnabled = enabledNamespaces.contains("mojang")
        val yarnEnabled = enabledNamespaces.contains("yarn")

        // region: Generic mappings lookups

        // Class
        command {
            name = "class"

            description = "Look up mappings info for a class, given a specific mappings namespace.\n\n" +

                    "**Namespaces:** $namespaceNames\n" +

                    if (yarnEnabled) {
                        "**Yarn channels:** " + YarnChannels.values().joinToString(", ") { "`${it.str}`" }
                    } else {
                        ""
                    } + "\n\n" +

                    "Instead of specifying a mappings namespace here, you can use the following commands to query " +
                    "each namespace directly: $classCommands.\n\n" +

                    "For more information or a list of versions for each namespace, you can use the following " +
                    "commands: $namespaceNames."

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

                    "**Namespaces:** $namespaceNames\n" +

                    if (yarnEnabled) {
                        "**Yarn channels:** " + YarnChannels.values().joinToString(", ") { "`${it.str}`" }
                    } else {
                        ""
                    } + "\n\n" +

                    "Instead of specifying a mappings namespace here, you can use the following commands to query " +
                    "each namespace directly: $fieldCommands.\n\n" +

                    "For more information or a list of versions for each namespace, you can use the following " +
                    "commands: $namespaceNames."

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

                    "**Namespaces:** $namespaceNames\n" +

                    if (yarnEnabled) {
                        "**Yarn channels:** " + YarnChannels.values().joinToString(", ") { "`${it.str}`" }
                    } else {
                        ""
                    } + "\n\n" +

                    "Instead of specifying a mappings namespace here, you can use the following commands to query " +
                    "each namespace directly: $methodCommands.\n\n" +

                    "For more information or a list of versions for each namespace, you can use the following " +
                    "commands: $namespaceNames."

            aliases = arrayOf("m")

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

        if (mcpEnabled) {
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
        }

        // endregion

        // region: Mojang mappings lookups

        if (mojangEnabled) {
            // Class
            command {
                name = "mmc"
                aliases = arrayOf("mojc", "mojmapc")

                description = "Look up Mojang mappings info for a class.\n\n" +

                        "For more information or a list of versions for Mojang mappings, you can use the `mojang` " +
                        "command."

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

                        "For more information or a list of versions for Mojang mappings, you can use the `mojang` " +
                        "command."

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

                        "For more information or a list of versions for Mojang mappings, you can use the `mojang` " +
                        "command."

                signature(::MojangArguments)

                action {
                    val args: MojangArguments

                    message.channel.withTyping {
                        args = parse(::MojangArguments)
                    }

                    queryMethods(MojangNamespace, args.query, args.version)
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

                        "**Yarn channels:** " + YarnChannels.values().joinToString(", ") { "`${it.str}`" } +
                        "\n\n" +

                        "For more information or a list of versions for Mojang mappings, you can use the `yarn` " +
                        "command."

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

                        "For more information or a list of versions for Mojang mappings, you can use the `yarn` " +
                        "command."

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

                        "For more information or a list of versions for Mojang mappings, you can use the `yarn` " +
                        "command."

                signature(::YarnArguments)

                action {
                    val args: YarnArguments

                    message.channel.withTyping {
                        args = parse(::YarnArguments)
                    }

                    queryMethods(YarnNamespace, args.query, args.version, args.yarnChannel)
                }
            }
        }

        // endregion

        // region: Mappings info commands

        if (mcpEnabled) {
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
        }

        if (mojangEnabled) {
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
        }

        if (yarnEnabled) {
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
        }

        // endregion

        logger.info { "Mappings extension set up - namespaces: " + enabledNamespaces.joinToString(", ") }
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

    private suspend fun getNamespaceNames(suffix: String? = null) =
        config.getEnabledNamespaces().joinToString(", ") { "`$it${suffix ?: ""}`" }

    private suspend fun getNamespaceCommands(suffix: String) =
        config.getEnabledNamespaces().joinToString(", ") {
            val prefix = when (it) {
                "mcp" -> "mcp"
                "mojang" -> "mm"
                "yarn" -> "y"

                else -> ""
            }

            "`$prefix$suffix`"
        }
}
