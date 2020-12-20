package com.kotlindiscord.kordex.ext.mappings.configuration.spec

import com.uchuhimo.konf.ConfigSpec
import dev.kord.common.entity.Snowflake

/** @suppress **/
object GuildsSpec : ConfigSpec() {
    val allowed by required<List<Snowflake>>()
    val banned by required<List<Snowflake>>()
}
