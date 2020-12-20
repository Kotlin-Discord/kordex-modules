package com.kotlindiscord.kordex.ext.mappings.configuration.spec

import com.uchuhimo.konf.ConfigSpec

/** @suppress **/
object SettingsSpec : ConfigSpec() {
    val namespaces by required<List<String>>()
}
