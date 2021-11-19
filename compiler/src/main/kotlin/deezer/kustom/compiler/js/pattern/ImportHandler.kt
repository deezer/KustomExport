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

package deezer.kustom.compiler.js.pattern

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeVariableName
import deezer.kustom.compiler.CompilerArgs
import deezer.kustom.compiler.firstParameterizedType
import deezer.kustom.compiler.js.ALL_KOTLIN_EXCEPTIONS
import deezer.kustom.compiler.js.ClassDescriptor
import deezer.kustom.compiler.js.InterfaceDescriptor
import deezer.kustom.compiler.js.TypeParameterDescriptor
import deezer.kustom.compiler.js.jsPackage
import deezer.kustom.compiler.js.mapping.EXCEPTION_JS_PACKAGE

// TODO: Use proper KotlinPoet %M and avoid this ugly trick
// https://square.github.io/kotlinpoet/#m-for-members

fun FileSpec.Builder.autoImport(
    origin: InterfaceDescriptor,
    concreteTypeParameters: List<TypeParameterDescriptor>
): FileSpec.Builder {
    return autoImport(
        origin.supers.map { it.origin.concreteTypeName } +
            origin.concreteTypeParameters.flatMap { listOf(it.origin.concreteTypeName) } +
            origin.properties.map { it.type.concreteTypeName } +
            origin.functions.flatMap { it.parameters }.map { it.type.concreteTypeName } +
            origin.functions.map { it.returnType.concreteTypeName },
        concreteTypeParameters
    )
}

fun FileSpec.Builder.autoImport(
    origin: ClassDescriptor,
    concreteTypeParameters: List<TypeParameterDescriptor>
): FileSpec.Builder {
    return autoImport(
        origin.supers.map { it.origin.concreteTypeName } +
            origin.concreteTypeParameters.flatMap { listOf(it.origin.concreteTypeName) } +
            origin.properties.map { it.type.concreteTypeName } +
            origin.functions.flatMap { it.parameters }.map { it.type.concreteTypeName } +
            origin.functions.map { it.returnType.concreteTypeName },
        concreteTypeParameters
    )
}

private fun FileSpec.Builder.autoImport(
    types: List<TypeName>,
    concreteTypeParameters: List<TypeParameterDescriptor>
): FileSpec.Builder {
    types.flatMap { type ->
        if (type is ParameterizedTypeName) {
            listOf(type, type.firstParameterizedType()) // TODO: limiting every classes to 1 generics
        } else {
            listOf(type)
        }
    }
        .distinct()
        .filter { it !is TypeVariableName } // Ignore generics
        .flatMap {
            when (it) {
                is ClassName -> listOf(it)
                is ParameterizedTypeName -> it.typeArguments.map {
                    it.cached(concreteTypeParameters).concreteTypeName
                } + it
                else -> emptyList()
            }
        }
        .map { it.asClassName() }
        // Ignore classes with package starting with "kotlin." and are not exceptions.
        .filterNot { it.isFromStdlib() && it !in ALL_KOTLIN_EXCEPTIONS }
        .forEach { className ->
            if (className in ALL_KOTLIN_EXCEPTIONS) { // Special case where we cannot generate these methods
                addAliasedImport(className, "Common${className.simpleName}")
                //if (packageName != EXCEPTION_JS.packageName) { // Useless import if same package
                //addImport(EXCEPTION_JS_PACKAGE, "import")
                addImport(EXCEPTION_JS_PACKAGE, "export")
                //}
            } else {
                addAliasedImport(className, "Common${className.simpleName}")
                if (CompilerArgs.erasePackage) return@forEach // No need to import ext func from the same package
                val jsPackage = className.packageName.jsPackage()
                // if (packageName != jsPackage) { // Useless import if same package
                addImport(jsPackage, "import${className.simpleName}")
                addImport(jsPackage, "export${className.simpleName}")
                // }
            }
        }
    return this
}

// Reduce the package name to give a distinct name when there is multiple ext func
// with the same name in different packages creating ambiguity
fun importExportPrefix(packageName: String): String {
    return packageName.split(".")
        .map { it.first() }
        .joinToString("")
}

private fun TypeName.isFromStdlib() =
    (this as? ClassName)?.packageName?.startsWith("kotlin")
        ?: (this as? ParameterizedTypeName)?.rawType?.packageName?.startsWith("kotlin")
        ?: false
