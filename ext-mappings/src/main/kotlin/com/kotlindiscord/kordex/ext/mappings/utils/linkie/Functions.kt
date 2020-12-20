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

package com.kotlindiscord.kordex.ext.mappings.utils.linkie

import me.shedaniel.linkie.*
import me.shedaniel.linkie.utils.remapMethodDescriptor

infix fun <T> T.hold(score: Double): ResultHolder<T> = ResultHolder(this, score)

fun MappingsContainer.toMetadata(): MappingsMetadata = MappingsMetadata(
    version = version,
    name = name,
    mappingSource = mappingSource,
    namespace = namespace ?: ""
)

fun <T> QueryResultCompound<T>.decompound(): QueryResult<T> = QueryResult(mappings.toMetadata(), value)

inline fun <T, V> QueryResultCompound<T>.map(transformer: (T) -> V): QueryResultCompound<V> {
    return QueryResultCompound(mappings, transformer(value))
}

inline fun <T, V> QueryResult<T>.map(transformer: (T) -> V): QueryResult<V> {
    return QueryResult(mappings, transformer(value))
}

fun MappingsProvider.get(): MappingsContainer = mappingsContainer!!.invoke()

fun String.isValidIdentifier(): Boolean {
    forEachIndexed { index, c ->
        if (index == 0) {
            if (!Character.isJavaIdentifierStart(c))
                return false
        } else {
            if (!Character.isJavaIdentifierPart(c))
                return false
        }
    }
    return isNotEmpty()
}

val Class.optimumName: String
    get() = mappedName ?: intermediaryName

val Field.optimumName: String
    get() = mappedName ?: intermediaryName

val Method.optimumName: String
    get() = mappedName ?: intermediaryName

fun Obf.buildString(nonEmptySuffix: String? = null): String =
    when {
        isEmpty() -> ""
        isMerged() -> merged!! + (nonEmptySuffix ?: "")
        else -> buildString {
            if (client != null) append("client=**$client**")
            if (server != null) append("server=**$server**")
            if (nonEmptySuffix != null) append(nonEmptySuffix)
        }
    }

inline fun String?.mapIfNotNullOrNotEquals(other: String, mapper: (String) -> String): String? =
    when {
        isNullOrEmpty() -> null
        this == other -> null
        else -> mapper(this)
    }

fun String.mapObfDescToNamed(container: MappingsContainer): String =
    remapMethodDescriptor { container.getClassByObfName(it)?.intermediaryName ?: it }

fun String.localiseFieldDesc(): String {
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

fun localisePrimitive(char: Char): String =
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
