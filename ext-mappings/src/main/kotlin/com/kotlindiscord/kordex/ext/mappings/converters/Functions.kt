package com.kotlindiscord.kordex.ext.mappings.converters

import com.kotlindiscord.kord.extensions.commands.parser.Arguments
import me.shedaniel.linkie.Namespace

/** Mappings version converter; see KordEx bundled functions for more info. **/
fun Arguments.mappingsVersion(displayName: String, namespaceGetter: suspend () -> Namespace) =
    arg(displayName, MappingsVersionConverter(namespaceGetter))

/** Mappings version converter; see KordEx bundled functions for more info. **/
fun Arguments.mappingsVersion(displayName: String, namespace: Namespace) =
    arg(displayName, MappingsVersionConverter { namespace })

/** Mappings namespace converter; see KordEx bundled functions for more info. **/
fun Arguments.namespace(displayName: String) =
    arg(displayName, NamespaceConverter())

/** Optional mappings version converter; see KordEx bundled functions for more info. **/
fun Arguments.optionalMappingsVersion(
    displayName: String,
    outputError: Boolean = false,
    namespaceGetter: suspend () -> Namespace
) =
    arg(displayName, MappingsVersionConverter(namespaceGetter).toOptional(outputError = outputError))

/** Optional mappings version converter; see KordEx bundled functions for more info. **/
fun Arguments.optionalMappingsVersion(displayName: String, outputError: Boolean = false, namespace: Namespace) =
    arg(displayName, MappingsVersionConverter { namespace }.toOptional(outputError = outputError))

/** Optional mappings namespace converter; see KordEx bundled functions for more info. **/
fun Arguments.optionalNamespace(displayName: String, outputError: Boolean = false) =
    arg(displayName, NamespaceConverter().toOptional(outputError = outputError))
