package com.kotlindiscord.kordex.utils

import com.kotlindiscord.kordex.utils.linkie.*
import me.shedaniel.linkie.MappingsContainer
import me.shedaniel.linkie.Namespace
import me.shedaniel.linkie.utils.mapFieldIntermediaryDescToNamed

private const val PAGE_SIZE = 3

fun methodsToPages(
    namespace: Namespace,
    mappings: MappingsContainer,
    result: QueryResultCompound<MethodResultSequence>
): List<String> {
    val pages = mutableListOf<String>()
    val methods = result.map { it.map { inner -> inner.value }.toList() }.value

    methods.chunked(PAGE_SIZE).forEach { result ->
        val page = result.map {
            val (clazz, method) = it
            var text = ""

            text += "**Method:** `${clazz.optimumName}::${method.optimumName}`\n"

            text += "**Name:** `" +
                    method.obfName.buildString("` -> ") +
                    "`" + method.intermediaryName + "`"
                    method.mappedName.mapIfNotNullOrNotEquals(method.intermediaryName) { name -> " -> `$name`" }

            text += "\n"

            if (namespace.supportsMixin()) {
                text += "\n"

                text += "**Mixin Target** ```" +
                        "L${clazz.optimumName}" +
                        method.optimumName +
                        (method.mappedDesc ?: method.intermediaryDesc.mapFieldIntermediaryDescToNamed(mappings)) +
                        "```"
            }

            if (namespace.supportsAT()) {
                text += "\n"

                text += "**Access Transformer** ```public" + clazz.optimumName.replace('/', '.') +
                        method.intermediaryName +
                        method.obfDesc.merged!!.mapObfDescToNamed(mappings) +
                        " # ${method.optimumName}```"
            } else if (namespace.supportsAW()) {
                text += "\n"

                text += "**Access Widener** ```accessible method ${clazz.optimumName} ${method.optimumName} " +
                        (method.mappedDesc ?: method.intermediaryDesc.mapFieldIntermediaryDescToNamed(mappings)) +
                        "```"
            }

            text
        }.joinToString("\n\n")

        pages.add(page)
    }

    return pages
}
