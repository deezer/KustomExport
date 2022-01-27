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

package deezer.kustomexport.compiler.js.pattern.enum

import com.squareup.kotlinpoet.ARRAY
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeSpec
import deezer.kustomexport.compiler.Logger
import deezer.kustomexport.compiler.js.EnumDescriptor
import deezer.kustomexport.compiler.js.jsExport
import deezer.kustomexport.compiler.js.jsPackage
import deezer.kustomexport.compiler.js.mapping.INDENTATION
import deezer.kustomexport.compiler.js.pattern.overrideGetterSetter

fun EnumDescriptor.transform() = transformEnum(this)

fun transformEnum(origin: EnumDescriptor): FileSpec {
    val jsClassPackage = origin.packageName.jsPackage()
    val originalClass = ClassName(origin.packageName, origin.classSimpleName)
    val jsExportedClass = ClassName(jsClassPackage, origin.classSimpleName)

    val commonClassSimpleName = "Common${origin.classSimpleName}"
    val delegateName = "common"

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
                .also { builder ->
                    origin.properties
                        // Don't export fields only present in super implementation
                        // .filterNot { p -> origin.supers.any { s -> s.parameters?.any { it.name == p.name } ?: false } }
                        .forEach {
                            if (it.name == "common") error("Cannot export an enum that use a property named 'common'. " +
                                "Please rename the property for something else (enum=${origin.classSimpleName})")
                            if (it.name != "name" && it.name != "ordinal") { // Kotlin keywords
                                builder.addProperty(
                                    overrideGetterSetter(
                                        prop = it,
                                        target = delegateName,
                                        import = false,
                                        forceOverride = false,
                                        isClassOpen = false,
                                        isClassThrowable = false
                                    )
                                )
                            }
                        }

                }
                .build()
        )
        .addFunction(
            FunSpec.builder("${origin.classSimpleName}_values")
                .addAnnotation(jsExport)
                .returns(ARRAY.parameterizedBy(jsExportedClass))
                .addCode(
                    "return·arrayOf(" +
                        origin.entries.joinToString { origin.generatedName(it) } +
                        ")"
                )
                .build()
        )
        .addFunction(
            FunSpec.builder("${origin.classSimpleName}_valueOf")
                .addAnnotation(jsExport)
                .returns(jsExportedClass.copy(nullable = true))
                .addParameter("name", STRING)
                .addCode(
                    origin.entries.joinToString("\n") {
                        "if (name == ${origin.generatedName(it)}.name)\n" +
                            "${INDENTATION}return·${origin.generatedName(it)}\n"
                    } + "\nreturn null"
                )
                .build()
        )
        .addFunction(
            FunSpec.builder("import${origin.classSimpleName}")
                .receiver(jsExportedClass)
                .returns(originalClass)
                .addStatement("return·$delegateName")
                .build()
        )
        .addFunction(
            FunSpec.builder("export${origin.classSimpleName}")
                .receiver(originalClass)
                .returns(jsExportedClass)
                .addStatement("return·${origin.classSimpleName}(this)")
                .build()
        )
        .also { b ->
            origin.entries.forEach { enumEntry ->
                b.addProperty(
                    PropertySpec.builder(origin.generatedName(enumEntry), jsExportedClass)
                        .addAnnotation(jsExport)
                        .initializer("$commonClassSimpleName.${enumEntry.name}.export${origin.classSimpleName}()")
                        .build()
                )
            }
        }
        .indent(INDENTATION)
        .build()
}
