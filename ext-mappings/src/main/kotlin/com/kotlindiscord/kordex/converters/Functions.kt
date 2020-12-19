package com.kotlindiscord.kordex.converters

import com.kotlindiscord.kord.extensions.commands.parser.Arguments
import me.shedaniel.linkie.Namespace

fun Arguments.mappingsVersion(displayName: String, namespaceGetter: suspend () -> Namespace) =
    arg(displayName, MappingsVersionConverter(namespaceGetter))

fun Arguments.mappingsVersion(displayName: String, namespace: Namespace) =
    arg(displayName, MappingsVersionConverter { namespace })

fun Arguments.namespace(displayName: String) =
    arg(displayName, NamespaceConverter())

fun Arguments.optionalMappingsVersion(
    displayName: String,
    outputError: Boolean = false,
    namespaceGetter: suspend () -> Namespace
) =
    arg(displayName, MappingsVersionConverter(namespaceGetter).toOptional(outputError = outputError))

fun Arguments.optionalMappingsVersion(displayName: String, outputError: Boolean = false, namespace: Namespace) =
    arg(displayName, MappingsVersionConverter { namespace }.toOptional(outputError = outputError))

fun Arguments.optionalNamespace(displayName: String, outputError: Boolean = false) =
    arg(displayName, NamespaceConverter().toOptional(outputError = outputError))
