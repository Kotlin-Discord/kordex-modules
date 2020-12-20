package com.kotlindiscord.kordex.ext.mappings

/**
 * Enum representing available Yarn channels.
 *
 * @property str String name used for the channel by Linkie
 */
enum class YarnChannels(val str: String) {
    LEGACY("legacy"),
    PATCHWORK("patchwork")
}