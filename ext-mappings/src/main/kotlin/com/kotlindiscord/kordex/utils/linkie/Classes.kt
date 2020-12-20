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

data class ResultHolder<T>(
    val value: T,
    val score: Double,
)

data class QueryResult<T>(
    val mappings: MappingsMetadata,
    val value: T,
)

data class MappingsMetadata(
    val version: String,
    val name: String,
    var mappingSource: MappingsContainer.MappingSource?,
    var namespace: String,
)

data class QueryContext(
    val provider: MappingsProvider,
    val searchKey: String,
)

data class QueryResultCompound<T>(
    val mappings: MappingsContainer,
    val value: T,
)

data class FieldResult(
    val parent: Class,
    val field: Field,
    val cm: QueryDefinition,
)

data class MethodResult(
    val parent: Class,
    val method: Method,
    val cm: QueryDefinition,
)
