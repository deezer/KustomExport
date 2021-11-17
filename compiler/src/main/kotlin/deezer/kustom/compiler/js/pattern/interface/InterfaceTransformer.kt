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

package deezer.kustom.compiler.js.pattern.`interface`

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import deezer.kustom.compiler.Logger
import deezer.kustom.compiler.js.InterfaceDescriptor
import deezer.kustom.compiler.js.MethodNameDisambiguation
import deezer.kustom.compiler.js.jsExport
import deezer.kustom.compiler.js.jsPackage
import deezer.kustom.compiler.js.mapping.INDENTATION
import deezer.kustom.compiler.js.pattern.autoImport
import deezer.kustom.compiler.js.pattern.buildWrapperClass
import deezer.kustom.compiler.js.pattern.buildWrappingFunction
import java.util.Locale

fun InterfaceDescriptor.transform() = transformInterface(this)

fun transformInterface(origin: InterfaceDescriptor): FileSpec {
    val originalClass = origin.asTypeName()

    val delegateName = origin.classSimpleName.replaceFirstChar { it.lowercase(Locale.getDefault()) }
    val jsClassPackage = origin.packageName.jsPackage()
    val jsExportedClass = ClassName(jsClassPackage, origin.classSimpleName)

    val importedClass = ClassName(jsClassPackage, "Imported${origin.classSimpleName}")
    val exportedClass = ClassName(jsClassPackage, "Exported${origin.classSimpleName}")

    return FileSpec.builder(jsClassPackage, origin.classSimpleName)
        .addAliasedImport(origin.asClassName, "Common${origin.classSimpleName}")
        .autoImport(origin)
        .addType(
            TypeSpec.interfaceBuilder(origin.classSimpleName) // ClassName(jsClassPackage, origin.classSimpleName).parameterizedBy(origin.generics.values.first()))
                /*.also { b ->
                    typeParametersMap.map { (_, exported) ->
                        b.addTypeVariable(TypeVariableName(exported))
                    }
                }*/
                .addModifiers(KModifier.EXTERNAL)
                .addAnnotation(jsExport)
                .also { builder ->
                    origin.supers.forEach { supr ->
                        val superType = supr.origin.concreteTypeName
                        if (superType is ClassName) {
                            val superClassName = ClassName(superType.packageName.jsPackage(), superType.simpleName)
                            builder.addSuperinterface(superClassName)
                        } else {
                            Logger.error("ClassTransformer - ${origin.classSimpleName} superTypes - ClassName($jsClassPackage, $superType)")
                        }
                    }

                    origin.properties.filter { it.isOverride.not() }.forEach { prop ->
                        val modifiers = if (prop.isOverride) listOf(KModifier.OVERRIDE) else emptyList()
                        builder.addProperty(
                            PropertySpec.builder(prop.name, prop.type.exportedTypeName, modifiers)
                                .mutable(prop.isMutable)
                                .build()
                        )
                    }

                    val mnd = MethodNameDisambiguation()
                    origin.functions
                        .filter { !it.isOverride }
                        .forEach { func ->
                            builder.addFunction(
                                func.buildWrappingFunction(
                                    body = false,
                                    import = false,
                                    delegateName = delegateName,
                                    mnd = mnd,
                                )
                            )
                        }
                }
                .build()
        )
        .addType(
            buildWrapperClass(
                delegateName = "exported",
                originalClass = originalClass,
                import = true,
                properties = origin.properties,
                functions = origin.functions,
            )
        )
        .addType(
            buildWrapperClass(
                delegateName = "common", // delegateName,
                originalClass = originalClass,
                import = false,
                properties = origin.properties,
                functions = origin.functions,
            )
        )
        .addFunction(
            FunSpec.builder("export${origin.classSimpleName}")
                /*.also { b ->
                    if (typeParametersMap.isNotEmpty()) {
                        typeParametersMap.forEach {
                            b.addTypeVariable(it.first)
                            b.addTypeVariable(it.second)
                        }
                    }
                }*/
                .receiver(originalClass)
                .returns(jsExportedClass)
                .addStatement(
                    "return (this as? ${importedClass.simpleName})?.exported ?: ${exportedClass.simpleName}(this)"
                )
                .build()
        )
        .addFunction(
            FunSpec.builder("import${origin.classSimpleName}")
                /*.also { b ->
                    if (typeParametersMap.isNotEmpty()) {
                        typeParametersMap.forEach {
                            b.addTypeVariable(it.first)
                            b.addTypeVariable(it.second)
                        }
                    }
                }*/
                .receiver(jsExportedClass)
                .returns(originalClass)
                .addStatement(
                    "return (this as? ${exportedClass.simpleName})?.common ?: ${importedClass.simpleName}(this)"
                )
                .build()
        )
        .indent(INDENTATION)
        .build()
}
