# Mappings Extension

[![Discord: Click here](https://img.shields.io/static/v1?label=Discord&message=Click%20here&color=7289DA&style=for-the-badge&logo=discord)](https://discord.gg/gjXqqCS) [![Release](https://img.shields.io/nexus/r/com.kotlindiscord.kordex.ext.mappings/ext-mappings?nexusVersion=3&logo=gradle&color=blue&label=Release&server=https%3A%2F%2Fmaven.kotlindiscord.com&style=for-the-badge)](https://maven.kotlindiscord.com/#browse/browse:maven-releases:com%2Fkotlindiscord%2Fkordex%2Fext%2Fmappings%2Fext-mappings) [![Snapshot](https://img.shields.io/nexus/s/com.kotlindiscord.kordex.ext.mappings/ext-mappings?logo=gradle&color=orange&label=Snapshot&server=https%3A%2F%2Fmaven.kotlindiscord.com&style=for-the-badge)](https://maven.kotlindiscord.com/#browse/browse:maven-snapshots:com%2Fkotlindiscord%2Fkordex%2Fext%2Fmappings%2Fext-mappings)

This module contains an extension written to provide Minecraft mappings information on Discord. It makes heavy use
of [linkie-core](https://github.com/shedaniel/linkie-core), which (as of this writing) supports MCP, Mojang and Yarn
mappings.

# Getting Started

* **Stable repo:** `https://maven.kotlindiscord.com/repository/maven-snapshots/`
* **Snapshot repo:** `https://maven.kotlindiscord.com/repository/maven-releases/`
* **Maven coordinates:** `com.kotlindiscord.kordex.ext.mappings:ext-mappings:VERSION`

At its simplest, you can add this extension directly to your bot with no further configuration. For example:

```kotlin
val bot = ExtensibleBot(
    token = System.getenv("TOKEN"),
    prefix = "!"
)

suspend fun main() {
    bot.addExtension(MappingsExtension::class)

    bot.start()
}
```

This will install the extension using its default configuration, which enables all mappings namespaces and does not
restrict the commands in any manner.

# Usage

This extension provides a number of commands for use on Discord.

* Commands for retrieving information about mappings namespaces: `mcp`, `mojang` and `yarn`
* MCP-specific lookup commands: `mcpc`, `mcpf` and `mcpm`
* Mojang-specific lookup commands: `mmc`, `mmf` and `mmm`
* Yarn-specific lookup commands: `yc`, `yf` and `ym`

# Configuration

* **Env var prefix:** `KORDX_MAPPINGS`
* **System property prefix:** `kordx.mappings`

This extension makes use of the Konf library for configuration. Included in the JAR is a default configuration file,
`kordex/mappings/default.toml`. You may configure the extension in one of the following ways:

* **TOML file as a resource:** `kordex/mappings/config.toml`
* **TOML file on the filesystem:** `config/ext/mappings.toml`
* **Environment variables,** prefixed with `KORDX_MAPPINGS_`, upper-casing keys and replacing `.` with `_` in key names
* **System properties,** prefixed with `kord.mappings.`

For an example, feel free to [read the included default.toml](src/main/resources/kordex/mappings/default.toml). The
following configuration keys are available:

* `categories.allowed`: List of categories mappings commands may be run within. When set, mappings commands may not be
  run in other categories, or in guild channels that aren't in categories. This setting takes priority over
  `categories.banned`.
* `categories.banned`: List of categories mappings commands may **not** be run within. When set, mappings commands may
  not be run within the given categories.
* `channels.allowed`: List of channels mappings commands may be run within. When set, mappings commands may not be run
  in other guild channels. This setting takes priority over `channels.banned`.
* `channels.banned`: List of channels mappings commands may **not** be run within. When set, mappings commands may not
  be run within the given channels.
* `guilds.allowed`: List of guilds mappings commands may be run within. When set, mappings commands may not be run in
  other guilds. This setting takes priority over `guilds.banned`.
* `guilds.banned`: List of guilds mappings commands may **not** be run within. When set, mappings commands may not be
  run within the given guilds.
* `settings.namespaces`: List of enabled namespaces. Currently, only `mcp`, `mojang` and `yarn` are supported, and
  they will all be enabled by default.

**Please note:** Mappings commands will always function when sent to the bot via a private message. However, only the
configured namespaces will be available - the user will not be able to query disabled namespaces.

# Customisation

This extension supports two primary methods of customization: Replacing the config adapter, and registering custom
checks. While the options do require some programming, they should help you to customize its behaviour to suit your
bot's needs.

## Custom Checks

[As described in the Kord Extensions documentation](https://kord-extensions.docs.kotlindiscord.com/concepts/checks/),
Kord Extensions provides a system of checks that can be applied to commands and other event handlers. Checks essentially
allow you to prevent execution of a command depending on the context it was executed within.

This extension allows you to register custom checks by calling the `MappingsExtension.addCheck()` function, as follows:

```kotlin
MappingsExtension.addCheck { command ->
    { event ->
        if (command == "yarn") {  // Only limit usage of the `yarn` command
            event.message.author?.id?.value != 109040264529608704L  // We don't want gdude using this
        } else {
            true
        }
    }
}
```

You can also write this using functions instead of lambdas, of course.

```kotlin
suspend fun mappingsCheck(command: String): suspend (MessageCreateEvent) -> Boolean {
    suspend fun inner(event: MessageCreateEvent): Boolean =
        if (command == "yarn") {  // Only limit usage of the `yarn` command
            event.message.author?.id?.value != 109040264529608704L  // We don't want gdude using this
        } else {
            true
        }

    return ::inner
}

MappingsExtension.addCheck(::mappingsCheck)
```

The approach you take is up to you!

## Replacing the Config Adapter

If you need some other form of configuration - for example, from a database - you can implement the
`MappingsConfigAdapater` interface in your own classes and pass an instance to `MappingsExtension.configure()` before
you start the bot to use it. While going into detail on each function is a little out of scope for this document, you
can find more information in the following places:

* [MappingsConfigAdapter interface](src/main/kotlin/com/kotlindiscord/kordex/ext/mappings/configuration/MappingsConfigAdapter.kt)
* [TomlMappingsConfig class](src/main/kotlin/com/kotlindiscord/kordex/ext/mappings/configuration/TomlMappingsConfig.kt)

# Licensing & Attribution

This extension makes use of [linkie-core](https://github.com/shedaniel/linkie-core), and contains code adapted
from [linkie-discord](https://github.com/shedaniel/linkie-discord). Both projects are licensed under the Apache License
2.0, which you can find in [LICENSE-linkie.md](LICENSE-linkie.md), distributed within the
`ext-mappings` JAR, on [the linkie-core GitHub](https://github.com/shedaniel/linkie-core/blob/master/LICENSE.md) or
on [the linkie-discord GitHub](https://github.com/shedaniel/linkie-discord/blob/master/LICENSE.md). Both
`linkie-core` and `linkie-discord` are property of [shedaniel](https://github.com/shedaniel).
