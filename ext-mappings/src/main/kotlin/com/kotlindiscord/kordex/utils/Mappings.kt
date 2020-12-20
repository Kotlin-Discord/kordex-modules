package com.kotlindiscord.kordex.utils

import com.kotlindiscord.kordex.utils.linkie.*
import me.shedaniel.linkie.MappingsContainer
import me.shedaniel.linkie.Namespace
import me.shedaniel.linkie.utils.mapFieldIntermediaryDescToNamed

private const val PAGE_SIZE = 3

fun classesToPages(
    namespace: Namespace,
    result: QueryResultCompound<ClassResultSequence>
): List<String> {
    val pages = mutableListOf<String>()
    val classes = result.map { it.map { inner -> inner.value }.toList() }.value

    classes.chunked(PAGE_SIZE).forEach { result ->
        val page = result.map { clazz ->
            var text = ""

            text += "**Class:** `${clazz.optimumName}`\n"

            text += "**Name:** `" +
                    clazz.obfName.buildString("` -> ") +
                    "`${clazz.intermediaryName}`" +
                    clazz.mappedName.mapIfNotNullOrNotEquals(clazz.intermediaryName) { name -> " -> `$name`" }

            text += "\n"

            if (namespace.supportsAT()) {
                text += "**Access Transformer:** ```public " +
                        clazz.intermediaryName.replace('/', '.') +
                        "```"
            } else if (namespace.supportsAW()) {
                text += "**Access Widener:** ```accessible class ${clazz.optimumName}```"
            }

            text
        }.joinToString("\n\n")

        pages.add(page)
    }

    return pages
}

fun fieldsToPages(
    namespace: Namespace,
    mappings: MappingsContainer,
    result: QueryResultCompound<FieldResultSequence>
): List<String> {
    val pages = mutableListOf<String>()
    val fields = result.map { it.map { inner -> inner.value }.toList() }.value

    fields.chunked(PAGE_SIZE).forEach { result ->
        val page = result.map {
            val (clazz, field) = it
            var text = ""

            text += "**Field:** `${clazz.optimumName}::${field.optimumName}`\n"

            text += "**Name:** `" +
                    field.obfName.buildString("` -> ") +
                    "`${field.intermediaryName}`" +
                    field.mappedName.mapIfNotNullOrNotEquals(field.intermediaryName) { name -> " -> `$name`" }

            if (namespace.supportsFieldDescription()) {
                text += "\n"

                text += "**Type:** `" +
                        (field.mappedDesc ?: field.intermediaryDesc.mapFieldIntermediaryDescToNamed(mappings))
                            .localiseFieldDesc() +
                        "`"
            }

            text += "\n"

            if (namespace.supportsMixin()) {
                text += "\n"

                text += "**Mixin Target:** ```" +
                        "L${clazz.optimumName};" +
                        field.optimumName +
                        ":" +
                        (field.mappedDesc ?: field.intermediaryDesc.mapFieldIntermediaryDescToNamed(mappings)) +
                        "```"
            }

            if (namespace.supportsAT()) {
                text += "\n"

                text += "**Access Transformer:** ```${field.intermediaryName} # ${field.optimumName}```"
            } else if (namespace.supportsAW()) {
                text += "\n"

                text += "**Access Widener:** ```accessible field ${clazz.optimumName} ${field.optimumName} " +
                        (field.mappedDesc ?: field.intermediaryDesc.mapFieldIntermediaryDescToNamed(mappings)) +
                        "```"
            }

            text
        }.joinToString("\n\n")

        pages.add(page)
    }

    return pages
}

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
