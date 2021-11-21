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

import com.google.devtools.ksp.containingFile
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeReference
import com.squareup.kotlinpoet.ANY
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.ksp.KotlinPoetKspPreview
import com.squareup.kotlinpoet.ksp.TypeParameterResolver
import com.squareup.kotlinpoet.ksp.toTypeName
import deezer.kustom.compiler.Logger
import deezer.kustom.compiler.js.mapping.isParameterizedAllowed
import java.io.File

val TypeName.qdot: String
    get() = if (this.isNullable) "?." else "."

fun TypeName.asClassName(): ClassName =
    (this as? ClassName)
        ?: (this as? ParameterizedTypeName)?.rawType
        ?: TODO("$this")

fun TypeName.removeTypeParameter(): TypeName =
    if (this is ParameterizedTypeName && !isParameterizedAllowed()) {
        rawType.copy(this.isNullable)
    } else this

private val regexFunctionX = Regex("kotlin\\.Function[0-9]+")
fun TypeName.isKotlinFunction() =
    this is ParameterizedTypeName && regexFunctionX.matches(this.rawType.canonicalName)

fun TypeName.packageName(): String =
    (this as? ClassName)?.packageName ?: (this as? ParameterizedTypeName)?.rawType?.packageName ?: TODO("$this")

fun TypeName.simpleName(): String =
    (this as? ClassName)?.simpleName ?: (this as? ParameterizedTypeName)?.rawType?.simpleName ?: TODO("$this")

// Issue in KSP: https://kotlinlang.slack.com/archives/C013BA8EQSE/p1633948867255800
// https://github.com/google/ksp/issues/728
// Instead of crashing during compilation, we try our best to guess the ClassName...
@KotlinPoetKspPreview
public fun KSTypeReference?.toTypeNamePatch(typeParamResolver: TypeParameterResolver): TypeName {
    // TODO : resolve().toTypeNamePatch()
    if (this == null) return ANY
    return try {
        toTypeName(typeParamResolver)
    } catch (e: Exception) {
        Logger.warn("cannot toTypeName = ${e.message} - $this")

        return guessClassFromImports(this.containingFile, classSimpleName = this.toString())
            ?: guessFromStdlib(
                this.toString(),
                *this.element!!.typeArguments.map { it.type.toTypeNamePatch(typeParamResolver) }.toTypedArray()
            )
            ?: ClassName(getPackageFromFile(this.containingFile), this.toString())
    }
}

// https://github.com/google/ksp/issues/728
@KotlinPoetKspPreview
public fun KSType?.toTypeNamePatch(typeParamResolver: TypeParameterResolver, containingFile: KSFile?): TypeName {
    if (this == null) return ANY
    //if (this.isError) return ANY
    return (
        try {
            toTypeName(typeParamResolver)
        } catch (e: Exception) {
            return guessClassFromImports(containingFile, classSimpleName = this.toString())
            // TODO handle support for stdlib
            /*?: guessFromStdlib(
                this.toString(),
                *declaration.typeParameters.map { ... }.toTypedArray()
            )*/
                ?: ClassName(declaration.packageName.asString(), declaration.simpleName.asString())
        }
        )
}

private fun getPackageFromFile(containingFile: KSFile?): String {
    // Best workaround so far, expect the import is visible in the file.
    val file = File(containingFile!!.filePath)
    file.readLines().forEach { line ->
        if (line.startsWith("package ")) return line.substringAfter("package ")
    }
    return "" // No package = default empty package
}

private fun guessClassFromImports(containingFile: KSFile?, classSimpleName: String): ClassName? {
    // Best workaround so far, expect the import is visible in the file.
    val file = File(containingFile?.filePath ?: return null)
    var resolvedPackage: String? = null
    file.readLines().forEach { line ->
        // It's missing multiple cases here:
        // - no idea if it's a generic class
        // - no idea if it's an aliased import
        // - no idea if it's just a suffix
        // - expecting KSTypeReference.toString() to return the class simpleName, it's not a hard contract
        if (line.startsWith("import ") && line.endsWith(".$classSimpleName")) {
            resolvedPackage = line.substringAfter("import ").substringBefore(".$classSimpleName")
        }
    }
    if (resolvedPackage != null) return ClassName(resolvedPackage!!, classSimpleName)
    return null
}

private fun guessFromStdlib(classSimpleName: String, vararg parameterizedTypes: TypeName): TypeName? {
    // if not available in the import, we check if it's a stdlib class, but here we don't have the package name, so relying on hardcoded list
    val knownStdLibClasses = mapOf<String, () -> TypeName>(
        "List" to {
            LIST.parameterizedBy(*parameterizedTypes)
        }
    )
    knownStdLibClasses[classSimpleName]?.let { return it.invoke() }

    return null
}
