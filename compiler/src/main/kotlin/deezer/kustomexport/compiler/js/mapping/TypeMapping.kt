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

@file:OptIn(KotlinPoetKspPreview::class)

package deezer.kustomexport.compiler.js.mapping

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.ksp.KotlinPoetKspPreview
import deezer.kustomexport.compiler.GenericsVisitor
import deezer.kustomexport.compiler.js.FormatString
import deezer.kustomexport.compiler.js.TypeParameterDescriptor
import deezer.kustomexport.compiler.js.jsPackage
import deezer.kustomexport.compiler.js.pattern.cached
import deezer.kustomexport.compiler.js.pattern.packageName
import deezer.kustomexport.compiler.js.pattern.qdot
import deezer.kustomexport.compiler.js.pattern.removeTypeParameter
import deezer.kustomexport.compiler.js.pattern.simpleName
import deezer.kustomexport.compiler.js.resolvedType
import deezer.kustomexport.compiler.js.toFormatString

class OriginTypeName(
    private val originTypeName: TypeName,
    private val typeParameters: List<TypeParameterDescriptor>,
    private val isKustomExportAnnotated: Boolean,
) {
    val concreteTypeName: TypeName by lazy { originTypeName.resolvedType(typeParameters) }
    fun importedMethod(name: FormatString) =
        TypeMapping.importMethod(name, concreteTypeName, typeParameters, isKustomExportAnnotated)

    val exportedTypeName: TypeName by lazy {
        // Remove TypeParameter: because we can't export generic in a cool manner yet, so we produce concrete from generics.
        // See @KustomExportGenerics
        TypeMapping.exportedType(concreteTypeName, typeParameters, isKustomExportAnnotated).removeTypeParameter()
    }

    fun exportedMethod(name: FormatString) =
        TypeMapping.exportMethod(name, concreteTypeName, typeParameters, isKustomExportAnnotated)

    fun portMethod(import: Boolean, name: FormatString) = if (import) importedMethod(name) else exportedMethod(name)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as OriginTypeName

        if (originTypeName != other.originTypeName) return false
        // Because TypeParameterDescriptor also contains an OriginTypeName
        //if (typeParameters != other.typeParameters) return false

        return true
    }

    override fun hashCode(): Int {
        var result = originTypeName.hashCode()
        // Because TypeParameterDescriptor also contains an OriginTypeName
        // result = 31 * result + typeParameters.hashCode()
        return result
    }

    companion object {
        private val allInstances = mutableMapOf<Pair<TypeName, List<TypeParameterDescriptor>>, OriginTypeName>()
        operator fun invoke(
            typeName: TypeName,
            typeParameters: List<TypeParameterDescriptor>
        ): OriginTypeName {
            val key = typeName to typeParameters
            allInstances[key]?.let { return it }
            allInstances[key] = OriginTypeName(typeName, typeParameters)
            return allInstances[key]!!
        }
    }
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
        val importMethod: (targetName: FormatString, TypeName, concreteTypeParameters: List<TypeParameterDescriptor>) -> FormatString, // Translates a domainType to an exportType
        val exportMethod: (targetName: FormatString, TypeName, concreteTypeParameters: List<TypeParameterDescriptor>) -> FormatString, // Translates an exportType to a domainType
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

    fun exportedType(
        origin: TypeName,
        concreteTypeParameters: List<TypeParameterDescriptor>,
        isKustomExportAnnotated: Boolean,
    ): TypeName {
        return getMapping(origin)?.exportType?.invoke(origin, concreteTypeParameters)
            ?.copy(nullable = origin.isNullable)
            ?: run {

                // Assuming @JsExport, but it may also be a type without annotation or handled by KotlinJs directly
                if (!isKustomExportAnnotated) return origin

                if (origin is TypeVariableName) {
                    // TODO: we may need to parse more info so that bounds don't get wrongly interpreted.
                    val exportedBounds = origin.bounds.map { it.cached(concreteTypeParameters, true).exportedTypeName }
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
                    val generics =
                        GenericsVisitor.resolvedGenerics.firstOrNull { it.originType == origin.rawType && it.typeParameters == origin.typeArguments }
                    return ClassName(
                        packageName = origin.rawType.packageName.jsPackage(),
                        simpleNames = listOf(generics?.exportName ?: origin.rawType.simpleName)
                    )
                        .parameterizedBy(
                            generics?.typeParameters?.map { it.cached(concreteTypeParameters).exportedTypeName }
                                ?: origin.typeArguments.map {
                                    it.cached(
                                        concreteTypeParameters,
                                        false
                                    ).exportedTypeName
                                }
                        )
                        .copy(nullable = origin.isNullable)
                }
                error("$origin (${origin::class.java}) is not supported yet. Please open an issue on our github.")
            }
    }

    fun exportMethod(
        targetName: FormatString,
        origin: TypeName,
        concreteTypeParameters: List<TypeParameterDescriptor>,
        isKustomExportAnnotated: Boolean
    ): FormatString {
        return getMapping(origin)?.exportMethod?.invoke(targetName, origin, concreteTypeParameters) ?: run {
            // Assuming @JsExport, but it may also be a type without annotation or handled by KotlinJs directly
            if (!isKustomExportAnnotated) return targetName


            // If no mapping, assume it's a project class, and it has a generated file
            val simpleName = origin.cached(concreteTypeParameters, true).exportedTypeName.simpleName()
            val exportMethodName = if (origin is TypeVariableName) {
                "export$simpleName"
            } else {
                "export$simpleName"
            }

            targetName + "${origin.qdot}%M()".toFormatString(
                MemberName(
                    origin.packageName().jsPackage(),
                    exportMethodName
                )
            )
        }
    }

    fun importMethod(
        targetName: FormatString,
        origin: TypeName,
        concreteTypeParameters: List<TypeParameterDescriptor>,
        isKustomExportAnnotated: Boolean
    ): FormatString {
        return getMapping(origin)?.importMethod?.invoke(targetName, origin, concreteTypeParameters) ?: run {
            // Assuming @JsExport, but it may also be a type without annotation or handled by KotlinJs directly
            if (!isKustomExportAnnotated) return targetName

            // If no mapping, assume it's a project class, and it has a generated file
            val simpleName = origin.cached(concreteTypeParameters, true).exportedTypeName.simpleName()
            val importMethodName = if (origin is TypeVariableName) {
                "import$simpleName"
            } else {
                "import$simpleName"
            }

            targetName + "${origin.qdot}%M()".toFormatString(
                MemberName(
                    origin.packageName().jsPackage(),
                    importMethodName
                )
            )
        }
    }
}
