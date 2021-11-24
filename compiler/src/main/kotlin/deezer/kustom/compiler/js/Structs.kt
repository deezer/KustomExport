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

package deezer.kustom.compiler.js

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeVariableName
import deezer.kustom.compiler.js.mapping.OriginTypeName
import kotlin.math.absoluteValue

data class PropertyDescriptor(
    val name: String,
    val type: OriginTypeName,
    val isMutable: Boolean,
    val isOverride: Boolean,
    // val namedArgs: List<String>, // Names of property arguments, for lambda (not sure if we want that here)
)

data class ParameterDescriptor(
    val name: String,
    val type: OriginTypeName,
) {
    val exportedMethod by lazy { type.exportedMethod(name.toFormatString()) }
    val importedMethod by lazy { type.importedMethod(name.toFormatString()) }
    inline fun portMethod(import: Boolean) = if (import) importedMethod else exportedMethod
}

data class FunctionDescriptor(
    val name: String,
    val isOverride: Boolean,
    val returnType: OriginTypeName,
    val parameters: List<ParameterDescriptor>,
)

data class SuperDescriptor(
    val origin: OriginTypeName,
    val parameters: List<ParameterDescriptor>?,
    val isSealed: Boolean,
)

data class TypeParameterDescriptor(
    val name: String,
    val origin: OriginTypeName,
)

sealed class Descriptor

data class InterfaceDescriptor(
    val packageName: String,
    val classSimpleName: String,
    val concreteTypeParameters: List<TypeParameterDescriptor>,
    val supers: List<SuperDescriptor>,
    val properties: List<PropertyDescriptor>,
    val functions: List<FunctionDescriptor>,
) : Descriptor() {
    // Useful for aliased imports (don't care about type parameters)
    val asClassName by lazy { ClassName(packageName, classSimpleName) }
    fun asTypeName() = ClassName(packageName, classSimpleName).let { className ->
        if (concreteTypeParameters.isNotEmpty()) {
            className.parameterizedBy(concreteTypeParameters.map { it.origin.concreteTypeName })
        } else className
    }
}

data class SealedClassDescriptor(
    val packageName: String,
    val classSimpleName: String,
    val supers: List<SuperDescriptor>,
    val constructorParams: List<ParameterDescriptor>,
    val properties: List<PropertyDescriptor>,
    val functions: List<FunctionDescriptor>,
    val subClasses: List<SealedSubClassDescriptor>,
) : Descriptor() {
    val asClassName by lazy { ClassName(packageName, classSimpleName) }
}

data class SealedSubClassDescriptor(
    val packageName: String,
    val classSimpleName: String,
) {
    val asClassName by lazy { ClassName(packageName, classSimpleName) }
}

data class ClassDescriptor(
    val packageName: String,
    val classSimpleName: String,
    val isOpen: Boolean,
    val concreteTypeParameters: List<TypeParameterDescriptor>,
    val supers: List<SuperDescriptor>,
    val constructorParams: List<ParameterDescriptor>,
    val properties: List<PropertyDescriptor>,
    val functions: List<FunctionDescriptor>,
) : Descriptor() {
    // Useful for aliased imports (don't care about type parameters)
    val asClassName by lazy { ClassName(packageName, classSimpleName) }
    fun asTypeName() = ClassName(packageName, classSimpleName).let { className ->
        if (concreteTypeParameters.isNotEmpty()) {
            className.parameterizedBy(concreteTypeParameters.map { it.origin.concreteTypeName })
        } else className
    }

    // Define a unique identifier to identify the class based on canonical name.
    val classIdHash by lazy { "${packageName}.$classSimpleName".hashCode().absoluteValue.toString(36) }
}

data class EnumDescriptor(
    val packageName: String,
    val classSimpleName: String,
    val entries: List<Entry>
) : Descriptor() {
    data class Entry(val name: String)
}

fun TypeName.resolvedType(typeParameters: List<TypeParameterDescriptor>?): TypeName {
    return if (this is ClassName) {
        this // Nothing to change here
    } else if (this is TypeVariableName) {
        typeParameters?.firstOrNull { name == it.name }
            ?.origin?.concreteTypeName?.copy(this.isNullable) ?: this
    } else if (this is ParameterizedTypeName) {
        if (typeArguments.any { it is TypeVariableName || it is ParameterizedTypeName }) {
            this.rawType.parameterizedBy(
                typeArguments.map { it.resolvedType(typeParameters) }
            ).copy(this.isNullable)
        } else this
    } else {
        this
    }
}