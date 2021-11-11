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
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeVariableName

// Describes format used between the parser and the writer
// Should avoid references to KSP or KotlinPoet so that we can easily swap to other tools if required, but
// describing a TypeName is complex enough that take some shortcuts.

data class PropertyDescriptor(
    val name: String,
    val type: TypeName,
    val isMutable: Boolean,
    val isOverride: Boolean,
    // val namedArgs: List<String>, // Names of property arguments, for lambda (not sure if we want that here)
)

data class ParameterDescriptor(
    val name: String,
    val type: TypeName,
)

data class FunctionDescriptor(
    val name: String,
    val isOverride: Boolean,
    val returnType: TypeName,
    val parameters: List<ParameterDescriptor>,
)

data class SuperDescriptor(
    val type: TypeName,
    val parameters: List<ParameterDescriptor>?,
)

sealed class Descriptor

data class InterfaceDescriptor(
    val packageName: String,
    val classSimpleName: String,
    val typeParameters: Map<String, TypeVariableName>,
    val supers: List<SuperDescriptor>,
    val properties: List<PropertyDescriptor>,
    val functions: List<FunctionDescriptor>,
) : Descriptor() {
    // Useful for aliased imports (don't care about type parameters)
    val asClassName by lazy { ClassName(packageName, classSimpleName) }
    fun asTypeName() = ClassName(packageName, classSimpleName).let {
        if (typeParameters.isNotEmpty()) {
            it.parameterizedBy(typeParameters.values.toList())
        } else it
    }
}

data class SealedClassDescriptor(
    val packageName: String,
    val classSimpleName: String,
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
    val typeParameters: Map<String, TypeVariableName>,
    val supers: List<SuperDescriptor>,
    val constructorParams: List<ParameterDescriptor>,
    val properties: List<PropertyDescriptor>,
    val functions: List<FunctionDescriptor>,
) : Descriptor() {
    // Useful for aliased imports (don't care about type parameters)
    fun asClassName() = ClassName(packageName, classSimpleName)
    fun asTypeName() = ClassName(packageName, classSimpleName).let {
        if (typeParameters.isNotEmpty()) {
            it.parameterizedBy(typeParameters.values.toList())
        } else it
    }
}

data class EnumDescriptor(
    val packageName: String,
    val classSimpleName: String,
    val entries: List<Entry>
) : Descriptor() {
    data class Entry(val name: String)
}
