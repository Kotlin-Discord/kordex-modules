package com.kotlindiscord.kordex.ext.mappings.utils

import com.kotlindiscord.kordex.ext.mappings.utils.linkie.buildString
import com.kotlindiscord.kordex.ext.mappings.utils.linkie.mapIfNotNullOrNotEquals
import me.shedaniel.linkie.MappingsContainer
import me.shedaniel.linkie.Namespace
import me.shedaniel.linkie.optimumName
import me.shedaniel.linkie.utils.*
import me.shedaniel.linkie.utils.MappingsQuery.localiseFieldDesc
import me.shedaniel.linkie.utils.MappingsQuery.mapObfDescToNamed

private const val PAGE_SIZE = 3

/** Given a set of result classes, format them into a list of pages for the paginator. **/
fun classesToPages(
    namespace: Namespace,
    queryResult: QueryResult<MappingsContainer, ClassResultSequence>
): List<Pair<String, String>> {
    val pages = mutableListOf<Pair<String, String>>()
    val classes = queryResult.map { it.map { inner -> inner.value }.toList() }.value

    classes.chunked(PAGE_SIZE).forEach { result ->
        val shortPage = result.joinToString("\n\n") { clazz ->
            var text = ""

            text += "**Class:** `${clazz.optimumName}`\n"

            text += "**Name:** `" +
                    clazz.obfName.buildString("` -> ") +
                    "`${clazz.intermediaryName}`" +
                    (clazz.mappedName.mapIfNotNullOrNotEquals(clazz.intermediaryName) { name -> " -> `$name`" } ?: "")

            text.trimEnd('\n')
        }

        val longPage = result.joinToString("\n\n") { clazz ->
            var text = ""

            text += "**Class:** `${clazz.optimumName}`\n"

            text += "**Name:** `" +
                    clazz.obfName.buildString("` -> ") +
                    "`${clazz.intermediaryName}`" +
                    (clazz.mappedName.mapIfNotNullOrNotEquals(clazz.intermediaryName) { name -> " -> `$name`" } ?: "")

            text += "\n"

            if (namespace.supportsAT()) {
                text += "**Access Transformer:** `public " +
                        clazz.intermediaryName.replace('/', '.') +
                        "`"
            } else if (namespace.supportsAW()) {
                text += "\n" +
                        "**Access Widener:** `accessible class ${clazz.optimumName}`"
            }

            text.trimEnd('\n')
        }

        pages.add(Pair(shortPage, longPage))
    }

    return pages
}

/** Given a set of result fields, format them into a list of pages for the paginator. **/
fun fieldsToPages(
    namespace: Namespace,
    mappings: MappingsContainer,
    queryResult: QueryResult<MappingsContainer, FieldResultSequence>
): List<Pair<String, String>> {
    val pages = mutableListOf<Pair<String, String>>()
    val fields = queryResult.map { it.map { inner -> inner.value }.toList() }.value

    fields.chunked(PAGE_SIZE).forEach { result ->
        val shortPage = result.joinToString("\n\n") {
            val (clazz, field) = it
            var text = ""

            text += "**Field:** `${clazz.optimumName}::${field.optimumName}`\n"

            text += "**Name:** `" +
                    field.obfName.buildString("` -> ") +
                    "`${field.intermediaryName}`" +
                    (field.mappedName.mapIfNotNullOrNotEquals(field.intermediaryName) { name -> " -> `$name`" } ?: "")

            if (namespace.supportsFieldDescription()) {
                text += "\n"

                text += "**Type:** `" +
                        (field.mappedDesc ?: field.intermediaryDesc.mapFieldIntermediaryDescToNamed(mappings))
                            .localiseFieldDesc() +
                        "`"
            }

            text
        }

        val longPage = result.joinToString("\n\n") {
            val (clazz, field) = it
            var text = ""

            text += "**Field:** `${clazz.optimumName}::${field.optimumName}`\n"

            text += "**Name:** `" +
                    field.obfName.buildString("` -> ") +
                    "`${field.intermediaryName}`" +
                    (field.mappedName.mapIfNotNullOrNotEquals(field.intermediaryName) { name -> " -> `$name`" } ?: "")

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

                text += "**Mixin Target:** `" +
                        "L${clazz.optimumName};" +
                        field.optimumName +
                        ":" +
                        (field.mappedDesc ?: field.intermediaryDesc.mapFieldIntermediaryDescToNamed(mappings)) +
                        "`"
            }

            if (namespace.supportsAT()) {
                text += "\n"

                text += "**Access Transformer:** `${field.intermediaryName} # ${field.optimumName}`"
            } else if (namespace.supportsAW()) {
                text += "\n"

                text += "**Access Widener:** `accessible field ${clazz.optimumName} ${field.optimumName} " +
                        (field.mappedDesc ?: field.intermediaryDesc.mapFieldIntermediaryDescToNamed(mappings)) +
                        "`"
            }

            text.trimEnd('\n')
        }

        pages.add(Pair(shortPage, longPage))
    }

    return pages
}

/** Given a set of result methods, format them into a list of pages for the paginator. **/
fun methodsToPages(
    namespace: Namespace,
    mappings: MappingsContainer,
    queryResult: QueryResult<MappingsContainer, MethodResultSequence>
): List<Pair<String, String>> {
    val pages = mutableListOf<Pair<String, String>>()
    val methods = queryResult.map { it.map { inner -> inner.value }.toList() }.value

    methods.chunked(PAGE_SIZE).forEach { result ->
        val shortPage = result.joinToString("\n\n") {
            val (clazz, method) = it
            var text = ""

            text += "**Method:** `${clazz.optimumName}::${method.optimumName}`\n"

            text += "**Name:** `" +
                    method.obfName.buildString("` -> ") +
                    "`" + method.intermediaryName + "`" +
                    (method.mappedName.mapIfNotNullOrNotEquals(method.intermediaryName) { name -> " -> `$name`" } ?: "")

            text.trimEnd('\n')
        }

        val longPage = result.joinToString("\n\n") {
            val (clazz, method) = it
            var text = ""

            text += "**Method:** `${clazz.optimumName}::${method.optimumName}`\n"

            text += "**Name:** `" +
                    method.obfName.buildString("` -> ") +
                    "`" + method.intermediaryName + "`" +
                    (method.mappedName.mapIfNotNullOrNotEquals(method.intermediaryName) { name -> " -> `$name`" } ?: "")

            text += "\n"

            if (namespace.supportsMixin()) {
                text += "\n"

                text += "**Mixin Target** `" +
                        "L${clazz.optimumName}" +
                        method.optimumName +
                        (method.mappedDesc ?: method.intermediaryDesc.mapFieldIntermediaryDescToNamed(mappings)) +
                        "`"
            }

            if (namespace.supportsAT()) {
                text += "\n"

                text += "**Access Transformer** `public" + clazz.optimumName.replace('/', '.') +
                        method.intermediaryName +
                        method.obfDesc.merged!!.mapObfDescToNamed(mappings) +
                        " # ${method.optimumName}`"
            } else if (namespace.supportsAW()) {
                text += "\n"

                text += "**Access Widener** `accessible method ${clazz.optimumName} ${method.optimumName} " +
                        (method.mappedDesc ?: method.intermediaryDesc.mapFieldIntermediaryDescToNamed(mappings)) +
                        "`"
            }

            text.trimEnd('\n')
        }

        pages.add(Pair(shortPage, longPage))
    }

    return pages
}
