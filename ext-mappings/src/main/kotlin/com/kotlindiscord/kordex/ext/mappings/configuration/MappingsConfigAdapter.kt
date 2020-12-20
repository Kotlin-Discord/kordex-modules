package com.kotlindiscord.kordex.ext.mappings.configuration

import dev.kord.common.entity.Snowflake

/**
 * Simple config adapter interface, which you can implement yourself if you need some kind of alternative config
 * backend.
 */
interface MappingsConfigAdapter {
    /** Get a list of category IDs mappings commands are explicitly allowed in. **/
    suspend fun getAllowedCategories(): List<Snowflake>

    /** Get a list of category IDs mappings commands are explicitly disallowed in. **/
    suspend fun getBannedCategories(): List<Snowflake>

    /** Get a list of channel IDs mappings commands are explicitly allowed in. **/
    suspend fun getAllowedChannels(): List<Snowflake>

    /** Get a list of channel IDs mappings commands are explicitly disallowed in. **/
    suspend fun getBannedChannels(): List<Snowflake>

    /** Get a list of guild IDs mappings commands are explicitly allowed in. **/
    suspend fun getAllowedGuilds(): List<Snowflake>

    /** Get a list of guild IDs mappings commands are explicitly disallowed in. **/
    suspend fun getBannedGuilds(): List<Snowflake>

    /** Get a list of enabled mappings namespaces. **/
    suspend fun getEnabledNamespaces(): List<String>
}
