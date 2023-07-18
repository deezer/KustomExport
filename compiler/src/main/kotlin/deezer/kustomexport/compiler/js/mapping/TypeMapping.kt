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

package deezer.kustomexport.compiler.js.mapping

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeVariableName
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
    private val concreteTypeParameters: List<TypeParameterDescriptor>,
    val isKustomExportAnnotated: Boolean,
    private val typeArgs: List<OriginTypeName>,
) {
    val concreteTypeName: TypeName by lazy { originTypeName.resolvedType(concreteTypeParameters) }
    fun importedMethod(name: FormatString) = importMethod(name)

    val exportedTypeName: TypeName by lazy {
        // Remove TypeParameter: because we can't export generic in a cool manner yet, so we produce concrete from generics.
        // See @KustomExportGenerics
        exportedType()
    }

    fun exportedMethod(name: FormatString) = exportMethod(name)

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

    private fun exportedType(): TypeName {
        val type = concreteTypeName
        return TypeMapping.getMapping(type)?.exportType?.invoke(type, concreteTypeParameters, typeArgs)
            ?.copy(nullable = type.isNullable)
            ?: run {
                // Assuming @JsExport, but it may also be a type without annotation or handled by KotlinJs directly
                if (!isKustomExportAnnotated) return type

                if (type is TypeVariableName) {
                    // TODO: we may need to parse more info so that bounds don't get wrongly interpreted.
                    val exportedBounds =
                        type.bounds.map { it.cached(concreteTypeParameters, true, typeArgs).exportedTypeName }
                    return TypeVariableName(
                        type.name,
                        exportedBounds
                    ).copy(nullable = type.isNullable)
                }
                if (type is ClassName) {
                    return ClassName(
                        packageName = type.packageName.jsPackage(),
                        simpleNames = listOf(type.simpleName)
                    ).copy(nullable = type.isNullable)
                }
                if (type is ParameterizedTypeName) {
                    val generics = GenericsVisitor.resolvedGenerics
                        .firstOrNull { it.originType == type.rawType && it.typeParameters == type.typeArguments }
                    return ClassName(
                        packageName = type.rawType.packageName.jsPackage(),
                        simpleNames = listOf(generics?.exportName ?: type.rawType.simpleName)
                    )
                        .parameterizedBy(
                            generics?.typeParameters?.map {
                                it.cached(concreteTypeParameters, typeArgs = typeArgs).exportedTypeName
                            }
                                ?: type.typeArguments.map {
                                    it.cached(
                                        concreteTypeParameters,
                                        false,
                                        typeArgs
                                    ).exportedTypeName
                                }
                        )
                        .copy(nullable = type.isNullable)
                }
                error("$type (${type::class.java}) is not supported yet. Please open an issue on our github.")
            }
    }

    private fun exportMethod(targetName: FormatString): FormatString {
        val type = concreteTypeName
        return TypeMapping.getMapping(type)?.exportMethod?.invoke(targetName, type, concreteTypeParameters, typeArgs)
            ?: run {
                // Assuming @JsExport, but it may also be a type without annotation or handled by KotlinJs directly
                if (!isKustomExportAnnotated) return targetName

                // If no mapping, assume it's a project class, and it has a generated file
                val simpleName = type.cached(concreteTypeParameters, true, typeArgs).exportedTypeName.simpleName()
                val exportMethodName = if (type is TypeVariableName) {
                    "export$simpleName"
                } else {
                    "export$simpleName"
                }

                targetName + "${type.qdot}%M()".toFormatString(
                    MemberName(
                        type.packageName().jsPackage(),
                        exportMethodName
                    )
                )
            }
    }

    fun importMethod(targetName: FormatString): FormatString {
        val type = concreteTypeName
        return TypeMapping.getMapping(type)?.importMethod?.invoke(targetName, type, concreteTypeParameters, typeArgs)
            ?: run {
                // Assuming @JsExport, but it may also be a type without annotation or handled by KotlinJs directly
                if (!isKustomExportAnnotated) return targetName

                // If no mapping, assume it's a project class, and it has a generated file
                val simpleName = type.cached(concreteTypeParameters, true, typeArgs).exportedTypeName.simpleName()
                val importMethodName = if (type is TypeVariableName) {
                    "import$simpleName"
                } else {
                    "import$simpleName"
                }

                targetName + "${type.qdot}%M()".toFormatString(
                    MemberName(
                        type.packageName().jsPackage(),
                        importMethodName
                    )
                )
            }
    }

    companion object {
        data class Key(
            val typeName: TypeName,
            val typeParameters: List<TypeParameterDescriptor>,
            val isKustomExportAnnotated: Boolean,
            val typeArgs: List<OriginTypeName>
        )

        private val allInstances = mutableMapOf<Key, OriginTypeName>()
        operator fun invoke(
            typeName: TypeName,
            typeParameters: List<TypeParameterDescriptor>,
            isKustomExportAnnotated: Boolean = false,
            typeArgs: List<OriginTypeName> = emptyList()
        ): OriginTypeName {
            val key = Key(
                typeName = typeName,
                typeParameters = typeParameters,
                isKustomExportAnnotated = isKustomExportAnnotated,
                typeArgs = typeArgs,
            )
            allInstances.getOrPut(key) {
                OriginTypeName(
                    originTypeName = typeName,
                    concreteTypeParameters = typeParameters,
                    isKustomExportAnnotated = isKustomExportAnnotated,
                    typeArgs = typeArgs
                )
            }
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
    data class MappingData(
        val targetName: FormatString,
        val typeName: TypeName,
        val typeArgs: List<TypeName>,
        val concreteTypeParameters: List<TypeParameterDescriptor>,
    )

    data class MappingOutput(
        val exportType: (typeName: TypeName, concreteTypeParameters: List<TypeParameterDescriptor>, typeArgs: List<OriginTypeName>) -> TypeName,
        // Translates a domainType to an exportType
        val importMethod: (targetName: FormatString, TypeName, concreteTypeParameters: List<TypeParameterDescriptor>, typeArgs: List<OriginTypeName>) -> FormatString,
        // Translates an exportType to a domainType
        val exportMethod: (targetName: FormatString, TypeName, concreteTypeParameters: List<TypeParameterDescriptor>, typeArgs: List<OriginTypeName>) -> FormatString,
    )

    public fun getMapping(origin: TypeName): MappingOutput? {
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
}
