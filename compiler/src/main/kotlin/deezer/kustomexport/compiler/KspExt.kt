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

package deezer.kustomexport.compiler

import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSFile
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.TypeName
import java.io.OutputStream
import kotlin.reflect.KProperty1

fun FileSpec.writeCode(environment: SymbolProcessorEnvironment, vararg sources: KSFile) {
    environment.codeGenerator.createNewFile(
        dependencies = Dependencies(aggregating = false, *sources),
        packageName = packageName,
        fileName = name
    ).use { outputStream ->
        outputStream.writer()
            .use {
                writeTo(it)
            }
    }
}

fun OutputStream.appendText(str: String) {
    this.write(str.toByteArray())
}

fun TypeName.firstParameterizedType() = (this as ParameterizedTypeName).typeArguments.first()
fun TypeName.secondParameterizedType() = (this as ParameterizedTypeName).typeArguments[1]

@Suppress("UNCHECKED")
fun <T> KSAnnotation.getArg(kProp: KProperty1<*, *>) =
    arguments.first { it.name?.asString() == kProp.name }.value as T
