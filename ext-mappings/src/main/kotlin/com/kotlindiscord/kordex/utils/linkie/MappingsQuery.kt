/*
 * Copyright (c) 2019, 2020 shedaniel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// These are Linkie classes and functions, they came like this.
@file:Suppress(
    "CollapsibleIfStatements",
    "DataClassShouldBeImmutable",
    "ExpressionBodySyntax",
    "MagicNumber",
    "MandatoryBracesIfStatements",
    "MaxLineLength",
    "MultiLineIfElse",
    "OptionalWhenBraces",
    "UndocumentedPublicClass",
    "UndocumentedPublicFunction",
    "UndocumentedPublicProperty",
    "UnnecessaryParentheses",
    "UnusedPrivateMember",
    "UseIfInsteadOfWhen",
)

package com.kotlindiscord.kordex.utils.linkie

import me.shedaniel.linkie.*
import me.shedaniel.linkie.utils.*

typealias ClassResultSequence = Sequence<ResultHolder<Class>>
typealias FieldResultSequence = Sequence<ResultHolder<Pair<Class, Field>>>
typealias MethodResultSequence = Sequence<ResultHolder<Pair<Class, Method>>>

object MappingsQuery {
    fun queryClasses(context: QueryContext): QueryResultCompound<ClassResultSequence> {
        val searchKey = context.searchKey
        val hasWildcard = searchKey == "*"

        val mappingsContainer = context.provider.get()
        val classes = mutableMapOf<Class, MatchResult>()

        mappingsContainer.classes.forEach { clazz ->
            if (hasWildcard) {
                classes[clazz] = MatchResult(true, null, null)
            } else if (!classes.contains(clazz)) {
                if (clazz.intermediaryName.containsOrMatchWildcard(searchKey).takeIf { it.matched }
                        ?.also { classes[clazz] = it }?.matched != true) {
                    if (clazz.mappedName.containsOrMatchWildcard(searchKey).takeIf { it.matched }
                            ?.also { classes[clazz] = it }?.matched != true) {
                        if (clazz.obfName.merged.containsOrMatchWildcard(searchKey).takeIf { it.matched }
                                ?.also { classes[clazz] = it }?.matched != true) {
                            if (clazz.obfName.server.containsOrMatchWildcard(searchKey).takeIf { it.matched }
                                    ?.also { classes[clazz] = it }?.matched != true) {
                                clazz.obfName.client.containsOrMatchWildcard(searchKey).takeIf { it.matched }
                                    ?.also { classes[clazz] = it }
                            }
                        }
                    }
                }
            }
        }

        if (classes.entries.isEmpty()) {
            if (searchKey.onlyClass().firstOrNull()?.isDigit() == true && !searchKey.onlyClass().isValidIdentifier()) {
                throw NullPointerException("No results found: `${searchKey.onlyClass()}` is not a valid java identifier")
            } else if (searchKey.startsWith("func_") || searchKey.startsWith("method_")) {
                throw NullPointerException("No results found: `$searchKey` looks like a method")
            } else if (searchKey.startsWith("field_")) {
                throw NullPointerException("No results found: `$searchKey` looks like a field")
            } else if ((!searchKey.startsWith("class_") && searchKey.firstOrNull()
                    ?.isLowerCase() == true) || searchKey.firstOrNull()?.isDigit() == true
            ) {
                throw NullPointerException("No results found: `$searchKey` doesn't look like a class")
            }
            throw NullPointerException("No results found")
        }

        val sortedClasses: Sequence<ResultHolder<Class>> = when {
            hasWildcard -> classes.entries.asSequence().sortedBy { it.key.intermediaryName }
                .mapIndexed { index, entry -> entry.key hold (classes.entries.size - index + 1) * 100.0 }
            else -> classes.entries.asSequence()
                .map { it.key hold (it.value.selfTerm?.similarityOnNull(it.value.matchStr) ?: 0.0) }
        }.sortedByDescending { it.score }
        return QueryResultCompound(mappingsContainer, sortedClasses)
    }

    fun queryFields(context: QueryContext): QueryResultCompound<FieldResultSequence> {
        val searchKey = context.searchKey
        val hasClass = searchKey.contains('/')
        val mappingsContainer = context.provider.get()
        val classes = mutableMapOf<Class, QueryDefinition>()

        if (hasClass) {
            val clazzKey = searchKey.substring(0, searchKey.lastIndexOf('/')).onlyClass()
            if (clazzKey == "*") {
                mappingsContainer.classes.forEach { classes[it] = QueryDefinition.WILDCARD }
            } else {
                mappingsContainer.classes.forEach { clazz ->
                    when {
                        clazz.intermediaryName.onlyClass().contains(clazzKey, true) -> classes[clazz] =
                            QueryDefinition.INTERMEDIARY
                        clazz.mappedName?.onlyClass()?.contains(clazzKey, true) == true -> classes[clazz] =
                            QueryDefinition.MAPPED
                        clazz.obfName.client?.onlyClass()?.contains(clazzKey, true) == true -> classes[clazz] =
                            QueryDefinition.OBF_CLIENT
                        clazz.obfName.server?.onlyClass()?.contains(clazzKey, true) == true -> classes[clazz] =
                            QueryDefinition.OBF_SERVER
                        clazz.obfName.merged?.onlyClass()?.contains(clazzKey, true) == true -> classes[clazz] =
                            QueryDefinition.OBF_MERGED
                    }
                }
            }
        } else mappingsContainer.classes.forEach { classes[it] = QueryDefinition.WILDCARD }
        val addedFields = mutableSetOf<Field>()
        val fields = mutableMapOf<FieldResult, QueryDefinition>()
        val fieldKey = searchKey.onlyClass('/')
        if (fieldKey == "*") {
            classes.forEach { (parent, cm) ->
                parent.fields.forEach { field ->
                    if (addedFields.add(field))
                        fields[FieldResult(parent, field, cm)] = QueryDefinition.WILDCARD
                }
            }
        } else {
            classes.forEach { (clazz, cm) ->
                clazz.fields.forEach { field ->
                    if (addedFields.add(field))
                        when {
                            field.intermediaryName.contains(fieldKey, true) -> fields[FieldResult(clazz, field, cm)] =
                                QueryDefinition.INTERMEDIARY
                            field.mappedName?.contains(fieldKey, true) == true -> fields[FieldResult(
                                clazz,
                                field,
                                cm
                            )] = QueryDefinition.MAPPED
                            field.obfName.client?.contains(fieldKey, true) == true -> fields[FieldResult(
                                clazz,
                                field,
                                cm
                            )] = QueryDefinition.OBF_CLIENT
                            field.obfName.server?.contains(fieldKey, true) == true -> fields[FieldResult(
                                clazz,
                                field,
                                cm
                            )] = QueryDefinition.OBF_SERVER
                            field.obfName.merged?.contains(fieldKey, true) == true -> fields[FieldResult(
                                clazz,
                                field,
                                cm
                            )] = QueryDefinition.OBF_MERGED
                        }
                }
            }
        }
        addedFields.clear()
        val sortedFields: Sequence<ResultHolder<FieldResult>> = when {
            fieldKey == "*" && (!hasClass || searchKey.substring(0, searchKey.lastIndexOf('/')).onlyClass() == "*") -> {
                // Class and field both wildcard
                fields.entries.asSequence().sortedBy { it.key.field.intermediaryName }.sortedBy {
                    it.key.parent.mappedName?.onlyClass() ?: it.key.parent.intermediaryName
                }.mapIndexed { index, entry -> entry.key hold (classes.entries.size - index + 1) * 100.0 }
            }
            fieldKey == "*" -> {
                // Only field wildcard
                val classKey = searchKey.substring(0, searchKey.lastIndexOf('/')).onlyClass()
                fields.entries.asSequence().sortedBy { it.key.field.intermediaryName }.map {
                    it.key hold when (it.key.cm) {
                        QueryDefinition.MAPPED -> it.key.parent.mappedName!!.onlyClass()
                        QueryDefinition.OBF_CLIENT -> it.key.parent.obfName.client!!.onlyClass()
                        QueryDefinition.OBF_SERVER -> it.key.parent.obfName.server!!.onlyClass()
                        QueryDefinition.OBF_MERGED -> it.key.parent.obfName.merged!!.onlyClass()
                        QueryDefinition.INTERMEDIARY -> it.key.parent.intermediaryName.onlyClass()
                        else -> null
                    }.similarityOnNull(classKey)
                }
            }
            hasClass && searchKey.substring(0, searchKey.lastIndexOf('/')).onlyClass() != "*" -> {
                // has class
                fields.entries.asSequence().sortedByDescending {
                    when (it.value) {
                        QueryDefinition.MAPPED -> it.key.field.mappedName!!
                        QueryDefinition.OBF_CLIENT -> it.key.field.obfName.client!!
                        QueryDefinition.OBF_SERVER -> it.key.field.obfName.server!!
                        QueryDefinition.OBF_MERGED -> it.key.field.obfName.merged!!
                        else -> it.key.field.intermediaryName
                    }.onlyClass().similarity(fieldKey)
                }.sortedBy {
                    it.key.parent.mappedName?.onlyClass() ?: it.key.parent.intermediaryName
                }.mapIndexed { index, entry -> entry.key hold (classes.entries.size - index + 1) * 100.0 }
            }
            else -> {
                fields.entries.asSequence().sortedBy {
                    it.key.parent.mappedName?.onlyClass() ?: it.key.parent.intermediaryName
                }.map {
                    it.key hold when (it.value) {
                        QueryDefinition.MAPPED -> it.key.field.mappedName!!
                        QueryDefinition.OBF_CLIENT -> it.key.field.obfName.client!!
                        QueryDefinition.OBF_SERVER -> it.key.field.obfName.server!!
                        QueryDefinition.OBF_MERGED -> it.key.field.obfName.merged!!
                        else -> it.key.field.intermediaryName
                    }.onlyClass().similarity(fieldKey)
                }
            }
        }

        if (fields.entries.isEmpty()) {
            if (searchKey.onlyClass().firstOrNull()?.isDigit() == true && !searchKey.onlyClass().isValidIdentifier()) {
                throw NullPointerException("No results found: `${searchKey.onlyClass()}` is not a valid java identifier")
            } else if (searchKey.startsWith("func_") || searchKey.startsWith("method_")) {
                throw NullPointerException("No results found: `$searchKey` looks like a method")
            } else if (searchKey.startsWith("class_")) {
                throw NullPointerException("No results found: `$searchKey` looks like a class")
            }
            throw NullPointerException("No results found")
        }

        val result: FieldResultSequence = sortedFields.map {
            it.value.parent to it.value.field hold it.score
        }.sortedByDescending { it.score }
        return QueryResultCompound(mappingsContainer, result)
    }

    fun queryMethods(context: QueryContext): QueryResultCompound<MethodResultSequence> {
        val searchKey = context.searchKey
        val hasClass = searchKey.contains('/')

        val mappingsContainer = context.provider.get()
        val classes = mutableMapOf<Class, QueryDefinition>()
        if (hasClass) {
            val clazzKey = searchKey.substring(0, searchKey.lastIndexOf('/')).onlyClass()
            if (clazzKey == "*") {
                mappingsContainer.classes.forEach { classes[it] = QueryDefinition.WILDCARD }
            } else {
                mappingsContainer.classes.forEach { clazz ->
                    when {
                        clazz.intermediaryName.onlyClass().contains(clazzKey, true) -> classes[clazz] =
                            QueryDefinition.INTERMEDIARY
                        clazz.mappedName?.onlyClass()?.contains(clazzKey, true) == true -> classes[clazz] =
                            QueryDefinition.MAPPED
                        clazz.obfName.client?.onlyClass()?.contains(clazzKey, true) == true -> classes[clazz] =
                            QueryDefinition.OBF_CLIENT
                        clazz.obfName.server?.onlyClass()?.contains(clazzKey, true) == true -> classes[clazz] =
                            QueryDefinition.OBF_SERVER
                        clazz.obfName.merged?.onlyClass()?.contains(clazzKey, true) == true -> classes[clazz] =
                            QueryDefinition.OBF_MERGED
                    }
                }
            }
        } else mappingsContainer.classes.forEach { classes[it] = QueryDefinition.WILDCARD }
        val addedMethods = mutableSetOf<Method>()
        val methods = mutableMapOf<MethodResult, QueryDefinition>()
        val methodKey = searchKey.onlyClass('/')
        if (methodKey == "*") {
            classes.forEach { (parent, cm) ->
                parent.methods.forEach { method ->
                    if (addedMethods.add(method))
                        methods[MethodResult(parent, method, cm)] = QueryDefinition.WILDCARD
                }
            }
        } else {
            classes.forEach { (parent, cm) ->
                parent.methods.forEach { method ->
                    if (addedMethods.add(method))
                        when {
                            method.intermediaryName.contains(methodKey, true) -> methods[MethodResult(
                                parent,
                                method,
                                cm
                            )] = QueryDefinition.INTERMEDIARY
                            method.mappedName?.contains(methodKey, true) == true -> methods[MethodResult(
                                parent,
                                method,
                                cm
                            )] = QueryDefinition.MAPPED
                            method.obfName.client?.contains(methodKey, true) == true -> methods[MethodResult(
                                parent,
                                method,
                                cm
                            )] = QueryDefinition.OBF_CLIENT
                            method.obfName.server?.contains(methodKey, true) == true -> methods[MethodResult(
                                parent,
                                method,
                                cm
                            )] = QueryDefinition.OBF_SERVER
                            method.obfName.merged?.contains(methodKey, true) == true -> methods[MethodResult(
                                parent,
                                method,
                                cm
                            )] = QueryDefinition.OBF_MERGED
                        }
                }
            }
        }
        addedMethods.clear()
        val sortedMethods: Sequence<ResultHolder<MethodResult>> = when {
            methodKey == "*" && (!hasClass || searchKey.substring(0, searchKey.lastIndexOf('/'))
                .onlyClass() == "*") -> {
                // Class and method both wildcard
                methods.entries.asSequence().sortedBy { it.key.method.intermediaryName }.sortedBy {
                    it.key.parent.mappedName?.onlyClass() ?: it.key.parent.intermediaryName
                }.mapIndexed { index, entry -> entry.key hold (classes.entries.size - index + 1) * 100.0 }
            }
            methodKey == "*" -> {
                // Only method wildcard
                val classKey = searchKey.substring(0, searchKey.lastIndexOf('/')).onlyClass()
                methods.entries.asSequence().sortedBy { it.key.method.intermediaryName }.map {
                    it.key hold when (it.key.cm) {
                        QueryDefinition.MAPPED -> it.key.parent.mappedName!!.onlyClass()
                        QueryDefinition.OBF_CLIENT -> it.key.parent.obfName.client!!.onlyClass()
                        QueryDefinition.OBF_SERVER -> it.key.parent.obfName.server!!.onlyClass()
                        QueryDefinition.OBF_MERGED -> it.key.parent.obfName.merged!!.onlyClass()
                        QueryDefinition.INTERMEDIARY -> it.key.parent.intermediaryName.onlyClass()
                        else -> null
                    }.similarityOnNull(classKey)
                }
            }
            hasClass && searchKey.substring(0, searchKey.lastIndexOf('/')).onlyClass() != "*" -> {
                // has class
                methods.entries.asSequence().sortedByDescending {
                    when (it.value) {
                        QueryDefinition.MAPPED -> it.key.method.mappedName!!
                        QueryDefinition.OBF_CLIENT -> it.key.method.obfName.client!!
                        QueryDefinition.OBF_SERVER -> it.key.method.obfName.server!!
                        QueryDefinition.OBF_MERGED -> it.key.method.obfName.merged!!
                        else -> it.key.method.intermediaryName
                    }.onlyClass().similarity(methodKey)
                }.sortedBy {
                    it.key.parent.mappedName?.onlyClass() ?: it.key.parent.intermediaryName
                }.mapIndexed { index, entry -> entry.key hold (classes.entries.size - index + 1) * 100.0 }
            }
            else -> {
                methods.entries.asSequence().sortedBy {
                    it.key.parent.mappedName?.onlyClass() ?: it.key.parent.intermediaryName
                }.map {
                    it.key hold when (it.value) {
                        QueryDefinition.MAPPED -> it.key.method.mappedName!!
                        QueryDefinition.OBF_CLIENT -> it.key.method.obfName.client!!
                        QueryDefinition.OBF_SERVER -> it.key.method.obfName.server!!
                        QueryDefinition.OBF_MERGED -> it.key.method.obfName.merged!!
                        else -> it.key.method.intermediaryName
                    }.onlyClass().similarity(methodKey)
                }
            }
        }

        if (methods.entries.isEmpty()) {
            if (searchKey.onlyClass().firstOrNull()?.isDigit() == true && !searchKey.onlyClass().isValidIdentifier()) {
                throw NullPointerException("No results found: `${searchKey.onlyClass()}` is not a valid java identifier")
            } else if (searchKey.startsWith("class_")) {
                throw NullPointerException("No results found: `$searchKey` looks like a class")
            } else if (searchKey.startsWith("field_")) {
                throw NullPointerException("No results found: `$searchKey` looks like a field")
            }
            throw NullPointerException("No results found")
        }

        val result: MethodResultSequence = sortedMethods.map {
            it.value.parent to it.value.method hold it.score
        }.sortedByDescending { it.score }
        return QueryResultCompound(mappingsContainer, result)
    }

    private fun String.mapObfDescToNamed(container: MappingsContainer): String =
        remapMethodDescriptor { container.getClassByObfName(it)?.intermediaryName ?: it }

    private fun String.localiseFieldDesc(): String {
        if (isEmpty()) return this
        if (length == 1) {
            return localisePrimitive(first())
        }
        val s = this
        var offset = 0
        for (i in s.indices) {
            if (s[i] == '[')
                offset++
            else break
        }
        if (offset + 1 == length) {
            val primitive = StringBuilder(localisePrimitive(first()))
            for (i in 1..offset) primitive.append("[]")
            return primitive.toString()
        }
        if (s[offset + 1] == 'L') {
            val substring = StringBuilder(substring(offset + 1))
            for (i in 1..offset) substring.append("[]")
            return substring.toString()
        }
        return s
    }

    private fun localisePrimitive(char: Char): String =
        when (char) {
            'Z' -> "boolean"
            'C' -> "char"
            'B' -> "byte"
            'S' -> "short"
            'I' -> "int"
            'F' -> "float"
            'J' -> "long"
            'D' -> "double"
            else -> char.toString()
        }
}
