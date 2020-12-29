package com.kotlindiscord.kordex.ext.mappings

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kordex.ext.mappings.configuration.MappingsConfigAdapter
import com.kotlindiscord.kordex.ext.mappings.configuration.TomlMappingsConfig
import dev.kord.core.event.message.MessageCreateEvent
import org.koin.dsl.bind
import org.koin.dsl.module

/** Set up the mappings extension and add it to the bot. **/
public fun ExtensibleBot.extMappings() {
    val config = koin.getOrNull<MappingsConfigAdapter>()

    if (config == null) {
        extMappingsConfig(TomlMappingsConfig())
    }

    this.addExtension(MappingsExtension::class)
}

/**
 *  Define the mappings extension configuration using a custom implementation.
 *
 *  Be sure to call this before [extMappings], or an exception will be thrown!
 */
@Throws(IllegalStateException::class)
public fun ExtensibleBot.extMappingsConfig(config: MappingsConfigAdapter) {
    val configObj = koin.getOrNull<MappingsConfigAdapter>()

    if (configObj != null) {
        error(
            "Mappings extension is already configured - make sure you call extMappingsConfig before extMappings!"
        )
    }

    val configModule = module {
        single { config } bind MappingsConfigAdapter::class
    }

    koin.loadModules(listOf(configModule))
}

/**
 * Add a check that will be run against all commands in the mappings extension.
 *
 * The function/lambda passed should take a [String] - the name of the command being checked - and return
 * a function taking a [MessageCreateEvent] and returning a [Boolean]. The returned function should return
 * `true` if the command should execute, and `false` if it shouldn't, using the command name and event as
 * context.
 *
 * Note that all custom checks will be run **BEFORE** checks that are specified in the configuration. Do not place
 * any actionable code in your checks - they should not interact with Discord aside from retrieving information from
 * it.
 */
public fun ExtensibleBot.extMappingsCheck(check: suspend (String) -> (suspend (MessageCreateEvent) -> Boolean)) =
    MappingsExtension.addCheck(check)
