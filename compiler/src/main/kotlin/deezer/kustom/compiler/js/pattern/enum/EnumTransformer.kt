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

package deezer.kustom.compiler.js.pattern.enum

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeSpec
import deezer.kustom.compiler.CompilerArgs
import deezer.kustom.compiler.js.EnumDescriptor
import deezer.kustom.compiler.js.jsExport
import deezer.kustom.compiler.js.jsPackage
import deezer.kustom.compiler.js.mapping.INDENTATION

fun EnumDescriptor.transform() = transformEnum(this)

fun transformEnum(origin: EnumDescriptor): FileSpec {
    val jsClassPackage = origin.packageName.jsPackage()
    val originalClass = ClassName(origin.packageName, origin.classSimpleName)
    val jsExportedClass = ClassName(jsClassPackage, origin.classSimpleName)

    val commonClassSimpleName = "Common${origin.classSimpleName}"
    val delegateName = "value"

    return FileSpec.builder(jsClassPackage, origin.classSimpleName)
        .addAliasedImport(originalClass, commonClassSimpleName)
        .addType(
            TypeSpec.classBuilder(origin.classSimpleName)
                .addAnnotation(jsExport)
                .primaryConstructor(
                    FunSpec.constructorBuilder()
                        .addParameter(delegateName, originalClass, KModifier.INTERNAL)
                        .addModifiers(KModifier.INTERNAL)
                        .build()
                )
                .addProperty(PropertySpec.builder(delegateName, originalClass).initializer(delegateName).build())
                .addProperty(PropertySpec.builder("name", STRING).initializer("$delegateName.name").build())
                .build()
        )
        .addFunction(
            FunSpec.builder("import${origin.classSimpleName}")
                .receiver(jsExportedClass)
                .returns(originalClass)
                .addStatement("return $delegateName")
                .build()
        )
        .addFunction(
            FunSpec.builder("export${origin.classSimpleName}")
                .receiver(originalClass)
                .returns(jsExportedClass)
                .addStatement("return ${origin.classSimpleName}(this)")
                .build()
        )
        .addType(
            TypeSpec.objectBuilder(origin.classSimpleName + "s")
                .addAnnotation(jsExport)
                .also { builder ->
                    origin.entries.forEach { enumEntry ->
                        builder.addProperty(
                            PropertySpec.builder(enumEntry.name, jsExportedClass)
                                .initializer("$commonClassSimpleName.${enumEntry.name}.export${origin.classSimpleName}()")
                                .build()
                        )
                    }
                }
                .build()
        )
        .indent(INDENTATION)
        .build()
}
