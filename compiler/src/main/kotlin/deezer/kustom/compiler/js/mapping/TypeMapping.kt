/*
 * Copyright 2021 Deezer.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package deezer.kustom.compiler.js.mapping

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeVariableName
import deezer.kustom.compiler.js.TypeParameterDescriptor
import deezer.kustom.compiler.js.jsPackage
import deezer.kustom.compiler.js.pattern.asClassName
import deezer.kustom.compiler.js.pattern.cached
import deezer.kustom.compiler.js.pattern.qdot
import deezer.kustom.compiler.js.pattern.removeTypeParameter
import deezer.kustom.compiler.js.resolvedType

// TODO: possible optimisation : re-use resolution based on a static/shared map
// val sharedMap: Map<TypeName, OriginTypeName>
// directly in a fake constructor
class OriginTypeName(
    private val originTypeName: TypeName,
    private val typeParameters: List<TypeParameterDescriptor>
) {
    val concreteTypeName: TypeName by lazy { originTypeName.resolvedType(typeParameters) }
    fun importedMethod(name: String) = TypeMapping.importMethod(name, concreteTypeName, typeParameters)
    val exportedTypeName by lazy { TypeMapping.exportedType(concreteTypeName, typeParameters).removeTypeParameter() }
    fun exportedMethod(name: String) = TypeMapping.exportMethod(name, concreteTypeName, typeParameters)
    fun portMethod(import: Boolean, name: String) = if (import) importedMethod(name) else exportedMethod(name)
}

object TypeMapping {
    val mappings = mutableMapOf<TypeName, MappingOutput>()
    val advancedMappings = mutableMapOf<(TypeName) -> Boolean, MappingOutput>()

    init {
        // TODO: make it dynamic to open-source the project and be extensible enough
        initCustomMapping()
    }

    // Mapped with the domain/origin type as key
    data class MappingOutput(
        val exportType: (typeName: TypeName, concreteTypeParameters: List<TypeParameterDescriptor>) -> TypeName,
        val importMethod: (targetName: String, TypeName, concreteTypeParameters: List<TypeParameterDescriptor>) -> String, // Translates a domainType to an exportType
        val exportMethod: (targetName: String, TypeName, concreteTypeParameters: List<TypeParameterDescriptor>) -> String, // Translates an exportType to a domainType
    )

    private fun getMapping(origin: TypeName): MappingOutput? {
        return when (origin) {
            // Simple mapping is defined for non-nullable type only,
            // nullable type follow the same transformations pattern than their non-null counterparts.
            // Advanced mappings requires the original type without transformation
            is ParameterizedTypeName -> {
                mappings[origin.rawType.copy(nullable = false)]
                    ?: advancedMappings.firstNotNullOfOrNull { if (it.key(origin)) it.value else null }
            }
            else ->
                mappings[origin.copy(nullable = false)]
                    ?: advancedMappings.firstNotNullOfOrNull { if (it.key(origin)) it.value else null }
        }
    }

    fun exportedType(origin: TypeName, concreteTypeParameters: List<TypeParameterDescriptor>): TypeName {
        return getMapping(origin)?.exportType?.invoke(origin, concreteTypeParameters)
            ?.copy(nullable = origin.isNullable)
            ?: run {
                // If no mapping, assume it's a project type, and it has a generated file.

                if (origin is TypeVariableName) {
                    val exportedBounds = origin.bounds.map { it.cached(concreteTypeParameters).exportedTypeName }
                    return TypeVariableName(
                        origin.name,
                        exportedBounds
                    ).copy(nullable = origin.isNullable)
                }
                if (origin is ClassName) {
                    return ClassName(
                        packageName = origin.packageName.jsPackage(),
                        simpleNames = listOf(origin.simpleName)
                    ).copy(nullable = origin.isNullable)
                }
                if (origin is ParameterizedTypeName) {
                    return ClassName(
                        packageName = origin.rawType.packageName.jsPackage(),
                        simpleNames = listOf(origin.rawType.simpleName)
                    )
                        .parameterizedBy(origin.typeArguments.map { it.cached(concreteTypeParameters).exportedTypeName })
                        .copy(nullable = origin.isNullable)
                }

                error("$origin (${origin::class.java}) is not supported yet. Please open an issue on our github.")
            }
    }

    fun exportMethod(
        targetName: String,
        origin: TypeName,
        concreteTypeParameters: List<TypeParameterDescriptor>
    ): String {
        return getMapping(origin)?.exportMethod?.invoke(targetName, origin, concreteTypeParameters) ?: run {
            // If no mapping, assume it's a project class, and it has a generated file
            if (origin is TypeVariableName) {
                "$targetName${origin.qdot}export${origin.bounds.first().asClassName().simpleName}()"
            } else {
                "$targetName${origin.qdot}export${origin.asClassName().simpleName}()"
            }
        }
    }

    fun importMethod(
        targetName: String,
        origin: TypeName,
        concreteTypeParameters: List<TypeParameterDescriptor>
    ): String {
        return getMapping(origin)?.importMethod?.invoke(targetName, origin, concreteTypeParameters) ?: run {
            // If no mapping, assume it's a project class, and it has a generated file
            if (origin is TypeVariableName) {
                "$targetName${origin.qdot}import${origin.bounds.first().asClassName().simpleName}()"
            } else {
                "$targetName${origin.qdot}import${origin.asClassName().simpleName}()"
            }
        }
    }
}
